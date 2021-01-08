package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;

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
                return WormholeConfig.basicEnergyCellCapacity;
            case ADVANCED:
                return WormholeConfig.advancedEnergyCellCapacity;
            case CREATIVE:
                return 100000000;
        }
        return 0;
    }

    public EnergyCellTile createTile(){
        switch(this){
            case BASIC:
                return new EnergyCellTile.BasicEnergyCellTile();
            case ADVANCED:
                return new EnergyCellTile.AdvancedEnergyCellTile();
            case CREATIVE:
                return new EnergyCellTile.CreativeEnergyCellTile();
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

    public Class<? extends EnergyCellTile> getTileEntityClass(){
        switch(this){
            case BASIC:
                return EnergyCellTile.BasicEnergyCellTile.class;
            case ADVANCED:
                return EnergyCellTile.AdvancedEnergyCellTile.class;
            case CREATIVE:
                return EnergyCellTile.CreativeEnergyCellTile.class;
        }
        return null;
    }

}
