package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public enum EnergyCellType {

    BASIC, ADVANCED, CREATIVE;

    public String getRegistryName(){
        return this.name().toLowerCase(Locale.ROOT) + "_energy_cell";
    }

    public int getCapacity(){
        switch(this){
            case BASIC:
                return WormholeConfig.basicEnergyCellCapacity.get();
            case ADVANCED:
                return WormholeConfig.advancedEnergyCellCapacity.get();
            case CREATIVE:
                return 100000000;
        }
        return 0;
    }

    public EnergyCellTile createTile(BlockPos pos, BlockState state){
        switch(this){
            case BASIC:
                return new EnergyCellTile.BasicEnergyCellTile(pos, state);
            case ADVANCED:
                return new EnergyCellTile.AdvancedEnergyCellTile(pos, state);
            case CREATIVE:
                return new EnergyCellTile.CreativeEnergyCellTile(pos, state);
        }
        return null;
    }

    public EnergyCellBlock getBlock(){
        switch(this){
            case BASIC:
                return Wormhole.basic_energy_cell;
            case ADVANCED:
                return Wormhole.advanced_energy_cell;
            case CREATIVE:
                return Wormhole.creative_energy_cell;
        }
        return null;
    }

    public BlockEntityType<EnergyCellTile> getTileEntityType(){
        switch(this){
            case BASIC:
                return Wormhole.basic_energy_cell_tile;
            case ADVANCED:
                return Wormhole.advanced_energy_cell_tile;
            case CREATIVE:
                return Wormhole.creative_energy_cell_tile;
        }
        return null;
    }

}
