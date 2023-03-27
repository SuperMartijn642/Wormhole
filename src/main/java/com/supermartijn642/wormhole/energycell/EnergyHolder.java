package com.supermartijn642.wormhole.energycell;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public interface EnergyHolder {

    int receiveEnergy(int maxReceive, boolean simulate);

    int extractEnergy(int maxExtract, boolean simulate);

    int getEnergyStored();

    void setEnergyStored(int energy);

    int getMaxEnergyStored();

    default boolean canReceive(){
        return true;
    }

    default boolean canExtract(){
        return true;
    }
}
