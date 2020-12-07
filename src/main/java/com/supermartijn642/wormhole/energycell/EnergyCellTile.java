package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.PortalGroupTile;
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
public class EnergyCellTile extends PortalGroupTile implements IEnergyStorage {

    public static class BasicEnergyCellTile extends EnergyCellTile {
        public BasicEnergyCellTile(){
            super(EnergyCellType.BASIC);
        }
    }

    public static class AdvancedEnergyCellTile extends EnergyCellTile {
        public AdvancedEnergyCellTile(){
            super(EnergyCellType.ADVANCED);
        }
    }

    public static class CreativeEnergyCellTile extends EnergyCellTile {
        public CreativeEnergyCellTile(){
            super(EnergyCellType.CREATIVE);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate){
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate){
            return maxExtract;
        }

        @Override
        public int getEnergyStored(){
            return this.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract(){
            return false;
        }

        @Override
        public void tick(){
            super.tick();
            for(Direction direction : Direction.values()){
                TileEntity tile = this.world.getTileEntity(this.pos.offset(direction));
                if(tile != null)
                    tile.getCapability(CapabilityEnergy.ENERGY).ifPresent(this::pushEnergy);
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(1, false);
        }
    }

    private final EnergyCellType type;
    protected int energy = 0;

    public EnergyCellTile(EnergyCellType type){
        super(type.getTileEntityType());
        this.type = type;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        if(maxReceive < 0)
            return -this.extractEnergy(-maxReceive, simulate);
        int absorb = Math.min(this.getMaxEnergyStored() - this.energy, maxReceive);
        if(!simulate){
            this.energy += absorb;
            if(absorb > 0)
                this.dataChanged();
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
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
    public int getEnergyStored(){
        return Math.min(this.energy, this.getMaxEnergyStored());
    }

    @Override
    public int getMaxEnergyStored(){
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
        if(cap == CapabilityEnergy.ENERGY)
            return LazyOptional.of(() -> this).cast();
        return super.getCapability(cap, side);
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
