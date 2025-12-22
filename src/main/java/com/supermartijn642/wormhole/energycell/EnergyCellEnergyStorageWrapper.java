package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 23/12/2025 by SuperMartijn642
 */
public class EnergyCellEnergyStorageWrapper extends SnapshotParticipant<Integer> implements EnergyStorage {

    private final IEnergyCellEntity entity;

    public EnergyCellEnergyStorageWrapper(IEnergyCellEntity entity){
        this.entity = entity;
    }

    @Override
    public long getAmount(){
        return this.entity.getEnergyStored(false);
    }

    @Override
    public long getCapacity(){
        return this.entity.getMaxEnergyStored(false);
    }

    @Override
    public long insert(long amount, TransactionContext transaction){
        StoragePreconditions.notNegative(amount);
        return this.entity.receiveEnergy((int)amount, false, transaction);
    }

    @Override
    public long extract(long amount, TransactionContext transaction){
        StoragePreconditions.notNegative(amount);
        return this.entity.extractEnergy((int)amount, false, transaction);
    }

    @Override
    protected Integer createSnapshot(){
        return this.entity.getEnergyStored(false);
    }

    @Override
    protected void readSnapshot(Integer snapshot){
        this.entity.setEnergyStored(snapshot);
    }

    @Override
    protected void onFinalCommit(){
        if(this.entity instanceof BaseBlockEntity)
            ((BaseBlockEntity)this.entity).dataChanged();
    }
}
