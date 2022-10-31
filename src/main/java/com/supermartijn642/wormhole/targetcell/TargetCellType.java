package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public enum TargetCellType {

    BASIC(WormholeConfig.basicTargetCellCapacity, () -> Wormhole.basic_target_cell, () -> Wormhole.basic_target_cell_tile, 4),
    ADVANCED(WormholeConfig.advancedTargetCellCapacity, () -> Wormhole.advanced_target_cell, () -> Wormhole.advanced_target_cell_tile, 8);

    private final Supplier<Integer> capacity;
    private final Supplier<TargetCellBlock> block;
    private final Supplier<BaseBlockEntityType<? extends TargetCellBlockEntity>> entityType;
    private final int visualCapacity;

    TargetCellType(Supplier<Integer> capacity, Supplier<TargetCellBlock> block, Supplier<BaseBlockEntityType<? extends TargetCellBlockEntity>> entityType, int visualCapacity){
        this.capacity = capacity;
        this.block = block;
        this.entityType = entityType;
        this.visualCapacity = visualCapacity;
    }

    public String getRegistryName(){
        return this.name().toLowerCase(Locale.ROOT) + "_target_cell";
    }

    public int getCapacity(){
        return this.capacity.get();
    }

    public TargetCellBlockEntity createTile(){
        return new TargetCellBlockEntity(this);
    }

    public TargetCellBlock getBlock(){
        return this.block.get();
    }

    public BaseBlockEntityType<? extends TargetCellBlockEntity> getBlockEntityType(){
        return this.entityType.get();
    }

    public int getVisualCapacity(){
        return visualCapacity;
    }
}
