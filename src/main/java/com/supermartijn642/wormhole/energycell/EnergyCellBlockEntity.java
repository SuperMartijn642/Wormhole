package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlockEntity extends PortalGroupBlockEntity implements IEnergyCellEntity {

    public static class BasicEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public BasicEnergyCellBlockEntity(){
            super(EnergyCellType.BASIC);
        }
    }

    public static class AdvancedEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public AdvancedEnergyCellBlockEntity(){
            super(EnergyCellType.ADVANCED);
        }
    }

    public static class CreativeEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public CreativeEnergyCellBlockEntity(){
            super(EnergyCellType.CREATIVE);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup){
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup){
            return maxExtract;
        }

        @Override
        public int getEnergyStored(boolean fromGroup){
            return this.getMaxEnergyStored();
        }

        @Override
        public int getMaxEnergyStored(boolean fromGroup){
            return this.type.getCapacity();
        }

        @Override
        public boolean canExtract(){
            return true;
        }

        @Override
        public void update(){
            super.update();
            for(Direction direction : Direction.values()){
                TileEntity entity = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if(entity != null)
                    //noinspection removal
                    entity.getCapability(CapabilityEnergy.ENERGY).ifPresent(this::pushEnergy);
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(this.getMaxEnergyStored(true), false);
        }
    }

    protected final EnergyCellType type;
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);
    protected int energy = 0;
    private int ticks = 40;

    public EnergyCellBlockEntity(EnergyCellType type){
        super(type.getBlockEntityType());
        this.type = type;
    }

    @Override
    public void update(){
        super.update();
        // Update block state
        this.ticks++;
        if(this.ticks >= 40){
            int maxEnergy = this.getMaxEnergyStored(true);
            int fillLevel = maxEnergy > 0 ? (int)Math.ceil((double)this.getEnergyStored(true) / maxEnergy * 15) : 0;
            if(this.getBlockState().getValue(EnergyCellBlock.ENERGY_LEVEL) != fillLevel){
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(EnergyCellBlock.ENERGY_LEVEL, fillLevel));
                this.ticks = 0;
            }
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().receiveEnergy(maxReceive, simulate);

        if(maxReceive < 0)
            return -this.extractEnergy(-maxReceive, simulate);
        int absorb = Math.min(this.getMaxEnergyStored(true) - this.energy, maxReceive);
        if(!simulate){
            this.energy += absorb;
            if(absorb > 0)
                this.dataChanged();
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup){
        if(maxExtract < 0)
            return -this.receiveEnergy(-maxExtract, simulate);
        int drain = Math.min(this.energy, maxExtract);
        if(!simulate){
            this.energy -= drain;
            if(drain > 0)
                this.dataChanged();
        }
        return drain;
    }

    @Override
    public int getEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getStoredEnergy();

        return Math.min(this.energy, this.getMaxEnergyStored(true));
    }

    @Override
    public int getMaxEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getEnergyCapacity();

        return this.type.getCapacity();
    }

    @Override
    public boolean canExtract(){
        return false;
    }

    @Override
    public boolean canReceive(){
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
        //noinspection removal
        if(cap == CapabilityEnergy.ENERGY)
            return this.energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.energyCapability.invalidate();
    }

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT tag = super.writeData();
        tag.putInt("energy", this.energy);
        return tag;
    }

    @Override
    protected void readData(CompoundNBT tag){
        super.readData(tag);
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;
    }
}
