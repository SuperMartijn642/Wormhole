package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public enum EnergyCellType {

    BASIC(WormholeConfig.basicEnergyCellCapacity, () -> Wormhole.basic_energy_cell, () -> Wormhole.basic_energy_cell_tile, EnergyCellBlockEntity.BasicEnergyCellBlockEntity::new),
    ADVANCED(WormholeConfig.advancedEnergyCellCapacity, () -> Wormhole.advanced_energy_cell, () -> Wormhole.advanced_energy_cell_tile, EnergyCellBlockEntity.AdvancedEnergyCellBlockEntity::new),
    CREATIVE(() -> 100000000, () -> Wormhole.creative_energy_cell, () -> Wormhole.creative_energy_cell_tile, EnergyCellBlockEntity.CreativeEnergyCellBlockEntity::new);

    private final Supplier<Integer> capacity;
    private final Supplier<EnergyCellBlock> block;
    private final Supplier<BaseBlockEntityType<? extends EnergyCellBlockEntity>> entityType;
    private final Supplier<EnergyCellBlockEntity> entitySupplier;

    EnergyCellType(Supplier<Integer> capacity, Supplier<EnergyCellBlock> block, Supplier<BaseBlockEntityType<? extends EnergyCellBlockEntity>> entityType, Supplier<EnergyCellBlockEntity> entitySupplier){
        this.capacity = capacity;
        this.block = block;
        this.entityType = entityType;
        this.entitySupplier = entitySupplier;
    }

    public String getRegistryName(){
        return this.name().toLowerCase(Locale.ROOT) + "_energy_cell";
    }

    public int getCapacity(){
        return this.capacity.get();
    }

    public EnergyCellBlockEntity createTile(){
        return this.entitySupplier.get();
    }

    public EnergyCellBlock getBlock(){
        return this.block.get();
    }

    public BaseBlockEntityType<? extends EnergyCellBlockEntity> getBlockEntityType(){
        return this.entityType.get();
    }
}
