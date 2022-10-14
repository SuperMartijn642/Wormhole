package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.Wormhole;

/**
 * Created 06/10/2022 by SuperMartijn642
 */
public class WormholeLootTableGenerator extends LootTableGenerator {

    public WormholeLootTableGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        this.dropSelf(Wormhole.portal_frame);
        this.dropSelf(Wormhole.portal_stabilizer);
        this.dropSelf(Wormhole.basic_energy_cell);
        this.dropSelf(Wormhole.advanced_energy_cell);
        this.dropSelf(Wormhole.creative_energy_cell);
        this.dropSelf(Wormhole.basic_target_cell);
        this.dropSelf(Wormhole.advanced_target_cell);
        this.dropSelf(Wormhole.coal_generator);
    }
}
