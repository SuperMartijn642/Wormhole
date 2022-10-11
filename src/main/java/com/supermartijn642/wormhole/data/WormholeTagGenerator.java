package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.wormhole.Wormhole;

/**
 * Created 06/10/2022 by SuperMartijn642
 */
public class WormholeTagGenerator extends TagGenerator {

    public WormholeTagGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        this.blockMineableWithPickaxe()
            .add(Wormhole.portal_frame)
            .add(Wormhole.portal_stabilizer)
            .add(Wormhole.basic_energy_cell)
            .add(Wormhole.advanced_energy_cell)
            .add(Wormhole.basic_target_cell)
            .add(Wormhole.advanced_target_cell)
            .add(Wormhole.coal_generator);
    }
}
