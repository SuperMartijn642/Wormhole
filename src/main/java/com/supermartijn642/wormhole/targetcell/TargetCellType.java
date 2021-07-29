package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public enum TargetCellType {

    BASIC, ADVANCED;

    public String getRegistryName(){
        return this.name().toLowerCase(Locale.ROOT) + "_target_cell";
    }

    public int getCapacity(){
        switch(this){
            case BASIC:
                return WormholeConfig.basicTargetCellCapacity.get();
            case ADVANCED:
                return WormholeConfig.advancedTargetCellCapacity.get();
        }
        return 0;
    }

    public TargetCellTile createTile(BlockPos pos, BlockState state){
        switch(this){
            case BASIC:
                return new TargetCellTile.BasicTargetCellTile(pos, state);
            case ADVANCED:
                return new TargetCellTile.AdvancedTargetCellTile(pos, state);
        }
        return null;
    }

    public TargetCellBlock getBlock(){
        switch(this){
            case BASIC:
                return Wormhole.basic_target_cell;
            case ADVANCED:
                return Wormhole.advanced_target_cell;
        }
        return null;
    }

    public BlockEntityType<TargetCellTile> getTileEntityType(){
        switch(this){
            case BASIC:
                return Wormhole.basic_target_cell_tile;
            case ADVANCED:
                return Wormhole.advanced_target_cell_tile;
        }
        return null;
    }

}
