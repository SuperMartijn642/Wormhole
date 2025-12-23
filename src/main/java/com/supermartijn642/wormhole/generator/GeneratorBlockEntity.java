package com.supermartijn642.wormhole.generator;

import com.mojang.serialization.Codec;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.portal.IPortalGroupEntity;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.*;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class GeneratorBlockEntity extends BaseBlockEntity implements TickableBlockEntity, EnergyHandler {

    private static final int BLOCKS_PER_TICK = 5;

    private final SnapshotJournal<Integer> snapshotJournal = new SnapshotJournal<Integer>() {
        @Override
        protected Integer createSnapshot(){
            return GeneratorBlockEntity.this.energy;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot){
            GeneratorBlockEntity.this.energy = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState){
            if(originalState != GeneratorBlockEntity.this.energy)
                GeneratorBlockEntity.this.dataChanged();
        }
    };

    protected int energy;
    protected final int energyCapacity;
    private final int energyRange;
    private final int energyTransferLimit;

    private final Set<BlockPos> portalBlocks = new LinkedHashSet<>();
    private final HashMap<BlockPos,Direction> energyBlocks = new HashMap<>();

    private int searchX, searchY, searchZ;

    public GeneratorBlockEntity(BaseBlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int energyCapacity, int energyRange, int energyTransferLimit){
        super(blockEntityType, pos, state);
        this.energyCapacity = energyCapacity;
        this.energyRange = energyRange;
        this.energyTransferLimit = energyTransferLimit;
        this.searchX = this.searchY = this.searchZ = -energyRange;
    }

    @Override
    public void update(){
        if(!this.level.isClientSide()){
            // find blocks with the energy capability
            for(int i = 0; i < BLOCKS_PER_TICK; i++){
                BlockPos pos = this.worldPosition.offset(this.searchX, this.searchY, this.searchZ);

                if(!pos.equals(this.worldPosition)){
                    BlockEntity entity = this.level.getBlockEntity(pos);
                    if(entity instanceof IPortalGroupEntity && ((IPortalGroupEntity)entity).hasGroup()){
                        if(!this.portalBlocks.contains(pos) || this.energyBlocks.containsKey(pos)){
                            this.portalBlocks.add(pos);
                            this.energyBlocks.remove(pos);
                            this.dataChanged();
                        }
                    }else{
                        boolean isEnergyHolder = false;
                        Direction inputSide = Direction.UP;
                        if(entity != null){
                            for(Direction side : Direction.values()){
                                EnergyHandler storage = this.level.getCapability(Capabilities.Energy.BLOCK, pos, null, entity, side);
                                if(storage != null){
                                    isEnergyHolder = true;
                                    inputSide = side;
                                    break;
                                }
                            }
                        }
                        if(isEnergyHolder && this.energyBlocks.get(pos) != inputSide){
                            this.energyBlocks.put(pos, inputSide);
                            this.dataChanged();
                        }else if(!isEnergyHolder && this.energyBlocks.containsKey(pos)){
                            this.energyBlocks.remove(pos);
                            this.dataChanged();
                        }
                        if(this.portalBlocks.contains(pos)){
                            this.portalBlocks.remove(pos);
                            this.dataChanged();
                        }
                    }
                }

                this.searchX++;
                if(this.searchX > this.energyRange){
                    this.searchX = -this.energyRange;
                    this.searchZ++;
                    if(this.searchZ > this.energyRange){
                        this.searchZ = -this.energyRange;
                        this.searchY++;
                        if(this.searchY > this.energyRange)
                            this.searchY = -this.energyRange;
                    }
                }
            }

            if(this.energy <= 0)
                return;

            // transfer energy
            int toTransfer = Math.min(this.energyTransferLimit, this.energy);
            Set<BlockPos> toRemove = new HashSet<>();
            for(BlockPos pos : this.portalBlocks){
                BlockEntity entity = this.level.getBlockEntity(pos);
                if(entity instanceof IPortalGroupEntity && ((IPortalGroupEntity)entity).hasGroup()){
                    PortalGroup group = ((IPortalGroupEntity)entity).getGroup();
                    try(Transaction transaction = Transaction.openRoot()){
                        int transferred = group.receiveEnergy(toTransfer, transaction);
                        transaction.commit();
                        toTransfer -= transferred;
                        this.energy -= transferred;
                        this.dataChanged();
                        if(this.energy == 0)
                            return;
                    }
                }else
                    toRemove.add(pos);
            }
            if(!toRemove.isEmpty()){
                this.portalBlocks.removeAll(toRemove);
                toRemove.clear();
                this.dataChanged();
            }
            for(Map.Entry<BlockPos,Direction> entry : this.energyBlocks.entrySet()){
                BlockPos pos = entry.getKey();
                EnergyHandler storage = this.level.getCapability(Capabilities.Energy.BLOCK, pos, entry.getValue());
                if(storage != null){
                    try(Transaction transaction = Transaction.openRoot()){
                        int transferred = storage.insert(toTransfer, transaction);
                        transaction.commit();
                        if(transferred > 0){
                            toTransfer -= transferred;
                            this.energy -= transferred;
                            this.dataChanged();
                            if(this.energy == 0)
                                return;
                        }
                    }
                }else
                    toRemove.add(pos);
            }
            if(!toRemove.isEmpty()){
                toRemove.forEach(this.energyBlocks::remove);
                this.dataChanged();
            }
        }
    }

    public Set<BlockPos> getChargingPortalBlocks(){
        return this.portalBlocks;
    }

    public Set<BlockPos> getChargingEnergyBlocks(){
        return this.energyBlocks.keySet();
    }

    @Override
    protected void writeData(ValueOutput output){
        output.putInt("energy", this.energy);
        BlockPos self = this.worldPosition;
        output.putInt("searchX", this.searchX - self.getX());
        output.putInt("searchY", this.searchY - self.getY());
        output.putInt("searchZ", this.searchZ - self.getZ());
        this.portalBlocks.stream().map(pos -> pos.subtract(self)).mapToLong(BlockPos::asLong).forEach(output.list("portalBlocks", Codec.LONG)::add);
        int[] energyBlocks = new int[this.energyBlocks.size() * 4];
        int index = 0;
        for(Map.Entry<BlockPos,Direction> entry : this.energyBlocks.entrySet()){
            energyBlocks[index++] = entry.getKey().getX() - self.getX();
            energyBlocks[index++] = entry.getKey().getY() - self.getY();
            energyBlocks[index++] = entry.getKey().getZ() - self.getZ();
            energyBlocks[index++] = entry.getValue().get3DDataValue();
        }
        output.putIntArray("energyBlocks", energyBlocks);
    }

    @Override
    protected void writeItemStackData(ValueOutput output){
        super.writeItemStackData(output);
        output.discard("searchX");
        output.discard("searchY");
        output.discard("searchZ");
        output.discard("portalBlocks");
        output.discard("energyBlocks");
    }

    @Override
    protected void readData(ValueInput input){
        this.energy = input.getIntOr("energy", 0);
        BlockPos self = this.worldPosition;
        this.searchX = Math.min(Math.max(input.getIntOr("searchX", 0) + self.getX(), -this.energyRange), this.energyRange);
        this.searchY = Math.min(Math.max(input.getIntOr("searchY", 0) + self.getY(), -this.energyRange), this.energyRange);
        this.searchZ = Math.min(Math.max(input.getIntOr("searchZ", 0) + self.getZ(), -this.energyRange), this.energyRange);
        this.portalBlocks.clear();
        input.listOrEmpty("portalBlocks", Codec.LONG).stream().map(BlockPos::of).map(pos -> pos.offset(self)).forEach(this.portalBlocks::add);
        this.energyBlocks.clear();
        int[] energyBlocks = input.getIntArray("energyBlocks").orElseGet(() -> new int[0]);
        for(int i = 0; i < energyBlocks.length / 4 * 4; )
            this.energyBlocks.put(
                new BlockPos(energyBlocks[i++] + self.getX(), energyBlocks[i++] + self.getY(), energyBlocks[i++] + self.getZ()),
                Direction.from3DDataValue(energyBlocks[i++])
            );
    }

    @Override
    public int insert(int amount, TransactionContext transaction){
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction){
        TransferPreconditions.checkNonNegative(amount);
        int extracted = Math.min(Math.min(this.energy, this.energyTransferLimit), amount);
        if(extracted > 0){
            this.snapshotJournal.updateSnapshots(transaction);
            this.energy -= extracted;
        }
        return extracted;
    }

    @Override
    public long getAmountAsLong(){
        return this.energy;
    }

    @Override
    public long getCapacityAsLong(){
        return this.energyCapacity;
    }
}
