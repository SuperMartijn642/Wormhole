package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellTile;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellTile extends PortalGroupTile implements IEnergyCellTile {

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
        public int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup){
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup){
            return maxExtract;
        }

        @Override
        public int getEnergyStored(boolean fromGroup){
            return this.getMaxEnergyStored(fromGroup);
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
            for(EnumFacing direction : EnumFacing.values()){
                TileEntity tile = this.world.getTileEntity(this.pos.offset(direction));
                if(tile != null){
                    IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
                    if(storage != null)
                        this.pushEnergy(storage);
                }
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(this.getMaxEnergyStored(true), false);
        }
    }

    protected final EnergyCellType type;
    protected int energy = 0;

    public EnergyCellTile(EnergyCellType type){
        super();
        this.type = type;
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
    public <T> T getCapability(Capability<T> cap, EnumFacing side){
        if(cap == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(this);
        return super.getCapability(cap, side);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound tag = super.writeData();
        tag.setInteger("energy", this.energy);
        return tag;
    }

    @Override
    protected void readData(NBTTagCompound tag){
        super.readData(tag);
        this.energy = tag.hasKey("energy") ? tag.getInteger("energy") : 0;
    }
}
