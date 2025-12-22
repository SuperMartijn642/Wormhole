package com.supermartijn642.wormhole.portal;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface IEnergyCellEntity {

    int receiveEnergy(int maxReceive, boolean fromGroup, TransactionContext transaction);

    int extractEnergy(int maxExtract, boolean fromGroup, TransactionContext transaction);

    int getEnergyStored(boolean fromGroup);

    void setEnergyStored(int energy);

    int getMaxEnergyStored(boolean fromGroup);
}
