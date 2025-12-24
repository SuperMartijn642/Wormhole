package com.supermartijn642.wormhole.portal;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface IEnergyCellEntity {

    int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup);

    int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup);

    int getEnergyStored(boolean fromGroup);

    void setEnergyStored(int energy);

    int getMaxEnergyStored(boolean fromGroup);
}
