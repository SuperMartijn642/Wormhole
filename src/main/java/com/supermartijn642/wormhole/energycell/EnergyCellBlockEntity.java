package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;

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
        public int receiveEnergy(int maxReceive, boolean fromGroup, TransactionContext transaction){
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean fromGroup, TransactionContext transaction){
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
                EnergyStorage storage = EnergyStorage.SIDED.find(this.level, this.worldPosition.relative(direction), direction.getOpposite());
                if(storage != null && storage.supportsInsertion()){
                    try(Transaction transaction = Transaction.openOuter()){
                        storage.insert(this.getMaxEnergyStored(true), transaction);
                    }
                }
            }
        }
    }

    private final SnapshotParticipant<Integer> snapshotParticipant = new SnapshotParticipant<Integer>() {
        @Override
        protected Integer createSnapshot(){
            return EnergyCellBlockEntity.this.energy;
        }

        @Override
        protected void readSnapshot(Integer snapshot){
            EnergyCellBlockEntity.this.energy = snapshot;
        }

        @Override
        protected void onFinalCommit(){
            EnergyCellBlockEntity.this.dataChanged();
        }
    };
    public final EnergyStorage energyHandler = new EnergyCellEnergyStorageWrapper(this);

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
    public int receiveEnergy(int maxReceive, boolean fromGroup, TransactionContext transaction){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().receiveEnergy(maxReceive, transaction);

        int absorb = Math.min(this.getMaxEnergyStored(true) - this.energy, maxReceive);
        if(absorb > 0){
            this.snapshotParticipant.updateSnapshots(transaction);
            this.energy += absorb;
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean fromGroup, TransactionContext transaction){
        if(!fromGroup && this.hasGroup()) // Don't allow extracting energy when the cell is part of a portal
            return 0;
        int drain = Math.min(this.energy, maxExtract);
        if(drain > 0){
            if(transaction != null)
                this.snapshotParticipant.updateSnapshots(transaction);
            this.energy -= drain;
            if(transaction == null)
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
