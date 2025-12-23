package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Created 23/12/2025 by SuperMartijn642
 */
public class EnergyCellEnergyHandlerWrapper extends SnapshotJournal<Integer> implements EnergyHandler {

    private final IEnergyCellEntity entity;

    public EnergyCellEnergyHandlerWrapper(IEnergyCellEntity entity){
        this.entity = entity;
    }

    @Override
    public long getAmountAsLong(){
        return this.entity.getEnergyStored(false);
    }

    @Override
    public long getCapacityAsLong(){
        return this.entity.getMaxEnergyStored(false);
    }

    @Override
    public int insert(int amount, TransactionContext transaction){
        TransferPreconditions.checkNonNegative(amount);
        return this.entity.receiveEnergy(amount, false, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction){
        TransferPreconditions.checkNonNegative(amount);
        return this.entity.extractEnergy(amount, false, transaction);
    }

    @Override
    protected Integer createSnapshot(){
        return this.entity.getEnergyStored(false);
    }

    @Override
    protected void revertToSnapshot(Integer snapshot){
        this.entity.setEnergyStored(snapshot);
    }

    @Override
    protected void onRootCommit(Integer originalState){
        if(originalState != this.entity.getEnergyStored(false) && this.entity instanceof BaseBlockEntity)
            ((BaseBlockEntity)this.entity).dataChanged();
    }
}
