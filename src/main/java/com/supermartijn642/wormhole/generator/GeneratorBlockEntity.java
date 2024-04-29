package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.energycell.EnergyHolder;
import com.supermartijn642.wormhole.portal.IPortalGroupEntity;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class GeneratorBlockEntity extends BaseBlockEntity implements TickableBlockEntity, EnergyHolder {

    private static final int BLOCKS_PER_TICK = 5;

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
        if(!this.level.isClientSide){
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
                        boolean isEnergyHolder = entity instanceof EnergyHolder && ((EnergyHolder)entity).canReceive();
                        Direction inputSide = Direction.UP;
                        if(!isEnergyHolder && entity != null && CommonUtils.isModLoaded("team_reborn_energy")){
                            BlockState state = entity.getBlockState();
                            for(Direction side : Direction.values()){
                                EnergyStorage storage = EnergyStorage.SIDED.find(this.level, pos, state, entity, side);
                                if(storage != null && storage.supportsInsertion()){
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
                    int transferred = group.receiveEnergy(toTransfer, false);
                    toTransfer -= transferred;
                    this.energy -= transferred;
                    this.dataChanged();
                    if(this.energy == 0)
                        return;
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
                BlockEntity entity = this.level.getBlockEntity(pos);
                if(entity instanceof EnergyHolder){
                    int transferred = ((EnergyHolder)entity).receiveEnergy(toTransfer, false);
                    toTransfer -= transferred;
                    this.energy -= transferred;
                    this.dataChanged();
                    if(this.energy == 0)
                        return;
                }else{
                    boolean isEnergyHolder = false;
                    if(entity != null && CommonUtils.isModLoaded("team_reborn_energy")){
                        BlockState state = entity.getBlockState();
                        EnergyStorage storage = EnergyStorage.SIDED.find(this.level, pos, state, entity, entry.getValue());
                        if(storage != null && storage.supportsInsertion()){
                            try(Transaction transaction = Transaction.openOuter()){
                                int transferred = (int)storage.insert(toTransfer, transaction);
                                this.energy -= transferred;
                                transaction.commit();
                                this.dataChanged();
                                if(this.energy == 0)
                                    return;
                            }
                            isEnergyHolder = true;
                        }
                    }
                    if(!isEnergyHolder)
                        toRemove.add(pos);
                }
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
    protected CompoundTag writeData(){
        CompoundTag data = new CompoundTag();
        data.putInt("energy", this.energy);
        BlockPos self = this.worldPosition;
        data.putInt("searchX", this.searchX - self.getX());
        data.putInt("searchY", this.searchY - self.getY());
        data.putInt("searchZ", this.searchZ - self.getZ());
        data.putLongArray("portalBlocks", this.portalBlocks.stream().map(pos -> pos.subtract(self)).map(BlockPos::asLong).collect(Collectors.toList()));
        int[] energyBlocks = new int[this.energyBlocks.size() * 4];
        int index = 0;
        for(Map.Entry<BlockPos,Direction> entry : this.energyBlocks.entrySet()){
            energyBlocks[index++] = entry.getKey().getX() - self.getX();
            energyBlocks[index++] = entry.getKey().getY() - self.getY();
            energyBlocks[index++] = entry.getKey().getZ() - self.getZ();
            energyBlocks[index++] = entry.getValue().get3DDataValue();
        }
        data.putIntArray("energyBlocks", energyBlocks);
        return data;
    }

    @Override
    protected CompoundTag writeItemStackData(){
        CompoundTag data = super.writeItemStackData();
        data.remove("searchX");
        data.remove("searchY");
        data.remove("searchZ");
        data.remove("portalBlocks");
        data.remove("energyBlocks");
        return data;
    }

    @Override
    protected void readData(CompoundTag tag){
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;
        BlockPos self = this.worldPosition;
        this.searchX = tag.contains("searchX") ? Math.min(Math.max(tag.getInt("searchX") + self.getX(), -this.energyRange), this.energyRange) : 0;
        this.searchY = tag.contains("searchY") ? Math.min(Math.max(tag.getInt("searchY") + self.getY(), -this.energyRange), this.energyRange) : 0;
        this.searchZ = tag.contains("searchZ") ? Math.min(Math.max(tag.getInt("searchZ") + self.getZ(), -this.energyRange), this.energyRange) : 0;
        this.portalBlocks.clear();
        if(tag.contains("portalBlocks", Tag.TAG_LONG_ARRAY))
            Arrays.stream(tag.getLongArray("portalBlocks")).mapToObj(BlockPos::of).map(pos -> pos.offset(self)).forEach(this.portalBlocks::add);
        this.energyBlocks.clear();
        if(tag.contains("energyBlocks", Tag.TAG_INT_ARRAY)){
            int[] energyBlocks = tag.getIntArray("energyBlocks");
            for(int i = 0; i < energyBlocks.length / 4 * 4; )
                this.energyBlocks.put(
                    new BlockPos(energyBlocks[i++] + self.getX(), energyBlocks[i++] + self.getY(), energyBlocks[i++] + self.getZ()),
                    Direction.from3DDataValue(energyBlocks[i++])
                );
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
        int extracted = Math.min(Math.min(this.energy, this.energyTransferLimit), maxExtract);
        if(extracted > 0 && !simulate){
            this.energy -= extracted;
            this.dataChanged();
        }
        return Math.max(extracted, 0);
    }

    @Override
    public int getEnergyStored(){
        return this.energy;
    }

    @Override
    public void setEnergyStored(int energy){
        this.energy = energy;
        this.dataChanged();
    }

    @Override
    public int getMaxEnergyStored(){
        return this.energyCapacity;
    }

    @Override
    public boolean canReceive(){
        return false;
    }
}
