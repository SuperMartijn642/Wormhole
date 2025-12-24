package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

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
            return this.getMaxEnergyStored(fromGroup);
        }

        @Override
        public int getMaxEnergyStored(boolean fromGroup){
            return this.type.getCapacity();
        }

        @Override
        public void update(){
            super.update();
            for(Direction direction : Direction.values()){
                BlockEntity entity = this.level.getBlockEntity(this.worldPosition.relative(direction));
                if(entity != null)
                    entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(this::pushEnergy);
            }
        }

        public void pushEnergy(IEnergyStorage energyStorage){
            if(energyStorage.canReceive())
                energyStorage.receiveEnergy(this.getMaxEnergyStored(true), false);
        }
    }

    protected final EnergyCellType type;
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> new EnergyCellEnergyStorageWrapper(this));
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

        int absorb = Math.min(this.getMaxEnergyStored(true) - this.energy, maxReceive);
        if(!simulate && absorb > 0){
            this.energy += absorb;
            this.dataChanged();
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup){
        if(!fromGroup && this.hasGroup()) // Don't allow extracting energy when the cell is part of a portal
            return 0;
        int drain = Math.min(this.energy, maxExtract);
        if(!simulate && drain > 0){
            this.energy -= drain;
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
    public void setEnergyStored(int energy){
        this.energy = energy;
    }

    @Override
    public int getMaxEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getEnergyCapacity();

        return this.type.getCapacity();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
        if(cap == ForgeCapabilities.ENERGY)
            return this.energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.energyCapability.invalidate();
    }

    @Override
    protected void writeData(ValueOutput output){
        super.writeData(output);
        output.putInt("energy", this.energy);
    }

    @Override
    protected void readData(ValueInput input){
        super.readData(input);
        this.energy = input.getIntOr("energy", 0);
    }
}
