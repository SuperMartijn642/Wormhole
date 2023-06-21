package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 21/06/2023 by SuperMartijn642
 */
public class WormholeAtlasSourceGenerator extends AtlasSourceGenerator {

    public WormholeAtlasSourceGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        this.blockAtlas().texturesFromModel("block/coal_generator_lit");
    }
}
