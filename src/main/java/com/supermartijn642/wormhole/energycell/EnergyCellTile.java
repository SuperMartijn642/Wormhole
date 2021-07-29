package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellTile;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellTile extends PortalGroupTile implements IEnergyCellTile {

    public static class BasicEnergyCellTile extends EnergyCellTile {

        public BasicEnergyCellTile(BlockPos pos, BlockState state){
            super(EnergyCellType.BASIC, pos, state);
        }
    }

    public static class AdvancedEnergyCellTile extends EnergyCellTile {

        public AdvancedEnergyCellTile(BlockPos pos, BlockState state){
            super(EnergyCellType.ADVANCED, pos, state);
        }
    }

    public static class CreativeEnergyCellTile extends EnergyCellTile {

        public CreativeEnergyCellTile(BlockPos pos, BlockState state){
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
        public void tick(){
            super.tick();
            for(Direction direction : Direction.values()){
                BlockEntity tile = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if(tile != null)
                    tile.getCapability(CapabilityEnergy.ENERGY).ifPresent(this::pushEnergy);
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(this.getMaxEnergyStored(true), false);
        }
    }

    protected final EnergyCellType type;
    protected int energy = 0;

    public EnergyCellTile(EnergyCellType type, BlockPos pos, BlockState state){
        super(type.getTileEntityType(), pos, state);
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
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
        if(cap == CapabilityEnergy.ENERGY)
            return LazyOptional.of(() -> this).cast();
        return super.getCapability(cap, side);
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
