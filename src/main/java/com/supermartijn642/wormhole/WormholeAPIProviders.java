package com.supermartijn642.wormhole;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class WormholeAPIProviders {

    public static void registerAPIProviders(){
        ItemStorage.SIDED.registerForBlockEntity((entity, side) -> entity.getItemCapability(), Wormhole.coal_generator_tile);
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> entity.energyHandler, Wormhole.stabilizer_tile);
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> entity.energyHandler, Wormhole.basic_energy_cell_tile);
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> entity.energyHandler, Wormhole.advanced_energy_cell_tile);
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> entity.energyHandler, Wormhole.creative_energy_cell_tile);
        EnergyStorage.SIDED.registerForBlockEntity((entity, direction) -> entity, Wormhole.coal_generator_tile);
    }
}
