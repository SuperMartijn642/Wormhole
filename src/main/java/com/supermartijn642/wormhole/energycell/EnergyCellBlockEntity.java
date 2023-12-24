package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlockEntity extends PortalGroupBlockEntity implements IEnergyCellEntity {

    public static class BasicEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public BasicEnergyCellBlockEntity(BlockPos pos, BlockState state){
            super(EnergyCellType.BASIC, pos, state);
        }
    }

    public static class AdvancedEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public AdvancedEnergyCellBlockEntity(BlockPos pos, BlockState state){
            super(EnergyCellType.ADVANCED, pos, state);
        }
    }

    public static class CreativeEnergyCellBlockEntity extends EnergyCellBlockEntity {

        public CreativeEnergyCellBlockEntity(BlockPos pos, BlockState state){
            super(EnergyCellType.CREATIVE, pos, state);
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
                IEnergyStorage storage = this.level.getCapability(Capabilities.EnergyStorage.BLOCK, this.worldPosition.relative(direction), direction.getOpposite());
                if(storage != null)
                    this.pushEnergy(storage);
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(this.getMaxEnergyStored(true), false);
        }
    }

    protected final EnergyCellType type;
    protected int energy = 0;
    private int ticks = 40;

    public EnergyCellBlockEntity(EnergyCellType type, BlockPos pos, BlockState state){
        super(type.getBlockEntityType(), pos, state);
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
    protected CompoundTag writeData(){
        CompoundTag tag = super.writeData();
        tag.putInt("energy", this.energy);
        return tag;
    }

    @Override
    protected void readData(CompoundTag tag){
        super.readData(tag);
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;
    }
}
