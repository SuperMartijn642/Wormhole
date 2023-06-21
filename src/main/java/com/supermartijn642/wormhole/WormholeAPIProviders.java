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
            RebornEnergyProviders.register();
    }

    private static class RebornEnergyProviders {
        public static void register(){
            EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> new EnergyHolderEnergyStorageWrapper(entity), Wormhole.stabilizer_tile);
            EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> new EnergyHolderEnergyStorageWrapper(entity), Wormhole.basic_energy_cell_tile);
            EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> new EnergyHolderEnergyStorageWrapper(entity), Wormhole.advanced_energy_cell_tile);
            EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> new EnergyHolderEnergyStorageWrapper(entity), Wormhole.coal_generator_tile);
        }
    }
}
