package com.supermartijn642.wormhole.energycell;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class EnergyHolderEnergyStorageWrapper extends SnapshotParticipant<Integer> implements EnergyStorage {

    private final EnergyHolder holder;

    public EnergyHolderEnergyStorageWrapper(EnergyHolder holder){
        this.holder = holder;
    }

    @Override
    protected Integer createSnapshot(){
        return this.holder.getEnergyStored();
    }

    @Override
    protected void readSnapshot(Integer snapshot){
        this.holder.setEnergyStored(snapshot);
    }

    @Override
    public boolean supportsInsertion(){
        return this.holder.canReceive();
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction){
        this.updateSnapshots(transaction);
        return this.holder.receiveEnergy((int)maxAmount, false);
    }

    @Override
    public boolean supportsExtraction(){
        return this.holder.canExtract();
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction){
        this.updateSnapshots(transaction);
        return this.holder.extractEnergy((int)maxAmount, false);
    }

    @Override
    public long getAmount(){
        return this.holder.getEnergyStored();
    }

    @Override
    public long getCapacity(){
        return this.holder.getMaxEnergyStored();
    }
}
