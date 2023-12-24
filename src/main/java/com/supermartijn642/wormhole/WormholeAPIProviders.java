package com.supermartijn642.wormhole;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class WormholeAPIProviders {

    public static void registerAPIProviders(){
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener((Consumer<RegisterCapabilitiesEvent>)event -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Wormhole.coal_generator_tile, (entity, side) -> entity);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, Wormhole.stabilizer_tile, (entity, side) -> entity);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, Wormhole.basic_energy_cell_tile, (entity, side) -> entity);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, Wormhole.advanced_energy_cell_tile, (entity, side) -> entity);
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, Wormhole.coal_generator_tile, (entity, side) -> entity);
        });
    }
}
