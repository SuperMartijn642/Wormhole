package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.portal.IPortalGroupEntity;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class GeneratorBlockEntity extends BaseBlockEntity implements TickableBlockEntity, IEnergyStorage {

    private static final int BLOCKS_PER_TICK = 5;

    protected int energy;
    protected final int energyCapacity;
    private final int energyRange;
    private final int energyTransferLimit;

    private final Set<BlockPos> portalBlocks = new LinkedHashSet<>();
    private final HashMap<BlockPos,EnumFacing> energyBlocks = new HashMap<>();

    private int searchX, searchY, searchZ;

    public GeneratorBlockEntity(BaseBlockEntityType<?> blockEntityType, int energyCapacity, int energyRange, int energyTransferLimit){
        super(blockEntityType);
        this.energyCapacity = energyCapacity;
        this.energyRange = energyRange;
        this.energyTransferLimit = energyTransferLimit;
        this.searchX = this.searchY = this.searchZ = -energyRange;
    }

    @Override
    public void update(){
        if(!this.world.isRemote){
            // find blocks with the energy capability
            for(int i = 0; i < BLOCKS_PER_TICK; i++){
                BlockPos pos = this.pos.add(this.searchX, this.searchY, this.searchZ);

                if(!pos.equals(this.pos)){
                    TileEntity entity = this.world.getTileEntity(pos);
                    if(entity instanceof IPortalGroupEntity && ((IPortalGroupEntity)entity).hasGroup()){
                        if(!this.portalBlocks.contains(pos) || this.energyBlocks.containsKey(pos)){
                            this.portalBlocks.add(pos);
                            this.energyBlocks.remove(pos);
                            this.dataChanged();
                        }
                    }else{
                        boolean isEnergyHolder = false;
                        EnumFacing inputSide = EnumFacing.UP;
                        if(entity != null){
                            for(EnumFacing side : EnumFacing.values()){
                                IEnergyStorage storage = entity.getCapability(CapabilityEnergy.ENERGY, side);
                                if(storage != null && storage.canReceive()){
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
                TileEntity entity = this.world.getTileEntity(pos);
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
            for(Map.Entry<BlockPos,EnumFacing> entry : this.energyBlocks.entrySet()){
                BlockPos pos = entry.getKey();
                TileEntity entity = this.world.getTileEntity(pos);
                IEnergyStorage storage;
                if(entity != null && (storage = entity.getCapability(CapabilityEnergy.ENERGY, entry.getValue())) != null){
                    final int max = toTransfer;
                    int transferred = storage.receiveEnergy(max, false);
                    toTransfer -= transferred;
                    this.energy -= transferred;
                    this.dataChanged();
                    if(this.energy == 0)
                        return;
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
    protected NBTTagCompound writeData(){
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("energy", this.energy);
        data.setInteger("searchX", this.searchX);
        data.setInteger("searchY", this.searchY);
        data.setInteger("searchZ", this.searchZ);
        int[] portalBlocks = new int[this.portalBlocks.size() * 3];
        int index = 0;
        for(BlockPos pos : this.portalBlocks){
            portalBlocks[index++] = pos.getX();
            portalBlocks[index++] = pos.getY();
            portalBlocks[index++] = pos.getZ();
        }
        data.setIntArray("portalBlocks", portalBlocks);
        int[] energyBlocks = new int[this.energyBlocks.size() * 4];
        index = 0;
        for(Map.Entry<BlockPos,EnumFacing> entry : this.energyBlocks.entrySet()){
            energyBlocks[index++] = entry.getKey().getX();
            energyBlocks[index++] = entry.getKey().getY();
            energyBlocks[index++] = entry.getKey().getZ();
            energyBlocks[index++] = entry.getValue().getIndex();
        }
        data.setIntArray("energyBlocks", energyBlocks);
        return data;
    }

    @Override
    protected void readData(NBTTagCompound tag){
        this.energy = tag.hasKey("energy") ? tag.getInteger("energy") : 0;
        this.searchX = tag.hasKey("searchX") ? Math.min(Math.max(tag.getInteger("searchX"), -this.energyRange), this.energyRange) : 0;
        this.searchY = tag.hasKey("searchY") ? Math.min(Math.max(tag.getInteger("searchY"), -this.energyRange), this.energyRange) : 0;
        this.searchZ = tag.hasKey("searchZ") ? Math.min(Math.max(tag.getInteger("searchZ"), -this.energyRange), this.energyRange) : 0;
        this.portalBlocks.clear();
        if(tag.hasKey("portalBlocks", Constants.NBT.TAG_INT_ARRAY)){
            int[] portalBlocks = tag.getIntArray("portalBlocks");
            for(int i = 0; i < portalBlocks.length / 3 * 3; )
                this.portalBlocks.add(new BlockPos(portalBlocks[i++], portalBlocks[i++], portalBlocks[i++]));
        }
        this.energyBlocks.clear();
        if(tag.hasKey("energyBlocks", Constants.NBT.TAG_INT_ARRAY)){
            int[] energyBlocks = tag.getIntArray("energyBlocks");
            for(int i = 0; i < energyBlocks.length / 4 * 4; )
                this.energyBlocks.put(new BlockPos(energyBlocks[i++], energyBlocks[i++], energyBlocks[i++]), EnumFacing.getFront(energyBlocks[i++]));
        }
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side){
        if(cap == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(this);
        return super.getCapability(cap, side);
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
    public int getMaxEnergyStored(){
        return this.energyCapacity;
    }

    @Override
    public boolean canExtract(){
        return true;
    }

    @Override
    public boolean canReceive(){
        return false;
    }
}
