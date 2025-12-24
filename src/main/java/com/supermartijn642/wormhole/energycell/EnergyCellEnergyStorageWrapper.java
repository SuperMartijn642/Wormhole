package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.portal.IEnergyCellEntity;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 23/12/2025 by SuperMartijn642
 */
public class EnergyCellEnergyStorageWrapper implements IEnergyStorage {

    private final IEnergyCellEntity entity;

    public EnergyCellEnergyStorageWrapper(IEnergyCellEntity entity){
        this.entity = entity;
    }

    @Override
    public int getEnergyStored(){
        return this.entity.getEnergyStored(false);
    }

    @Override
    public int getMaxEnergyStored(){
        return this.entity.getMaxEnergyStored(false);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        if(maxReceive < 0)
            throw new IllegalArgumentException("Insertion amount must not be negative!");
        return this.entity.receiveEnergy(maxReceive, simulate, false);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
        if(maxExtract < 0)
            throw new IllegalArgumentException("Extraction amount must not be negative!");
        return this.entity.extractEnergy(maxExtract, simulate, false);
    }

    @Override
    public boolean canExtract(){
        return true;
    }

    @Override
    public boolean canReceive(){
        return true;
    }
}
