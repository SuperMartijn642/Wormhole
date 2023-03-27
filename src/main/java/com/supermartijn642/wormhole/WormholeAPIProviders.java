package com.supermartijn642.wormhole;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.wormhole.energycell.EnergyHolderEnergyStorageWrapper;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class WormholeAPIProviders {

    public static void registerAPIProviders(){
        if(CommonUtils.isModLoaded("team_reborn_energy"))
            registerEnergyProviders();
    }

    private static void registerEnergyProviders(){
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> new EnergyHolderEnergyStorageWrapper((StabilizerBlockEntity)entity), Wormhole.stabilizer_tile);
    }
}
