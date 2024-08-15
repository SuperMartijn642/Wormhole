package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.NBTRecipe;
import com.supermartijn642.wormhole.Wormhole;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.item.Items;

/**
 * Created 06/10/2022 by SuperMartijn642
 */
public class WormholeRecipeGenerator extends RecipeGenerator {

    public WormholeRecipeGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        // Portal frame
        this.shaped(Wormhole.portal_frame, 2)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.COBBLESTONES)
            .input('C', Items.OBSIDIAN)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);

        // Portal stabilizer
        this.shaped(Wormhole.portal_stabilizer)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("AEA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.STORAGE_BLOCKS_LAPIS)
            .input('C', ConventionalItemTags.GLOWSTONE_DUSTS)
            .input('D', ConventionalItemTags.ENDER_PEARLS)
            .input('E', ConventionalItemTags.STORAGE_BLOCKS_REDSTONE)
            .unlockedBy(ConventionalItemTags.ENDER_PEARLS);

        // Basic energy cell
        this.shaped(Wormhole.basic_energy_cell)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.REDSTONE_DUSTS)
            .input('C', ConventionalItemTags.STORAGE_BLOCKS_REDSTONE)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);

        // Advanced energy cell
        this.shaped(Wormhole.advanced_energy_cell)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.QUARTZ_GEMS)
            .input('C', Items.REPEATER)
            .input('D', Wormhole.basic_energy_cell)
            .unlockedBy(Wormhole.basic_energy_cell);

        // Basic target cell
        this.shaped(Wormhole.basic_target_cell)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.LAPIS_GEMS)
            .input('C', Items.COMPASS)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);

        // Advanced target cell
        this.shaped(Wormhole.advanced_target_cell)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.LAPIS_GEMS)
            .input('C', ConventionalItemTags.GLOWSTONE_DUSTS)
            .input('D', Wormhole.basic_target_cell)
            .unlockedBy(Wormhole.basic_target_cell);

        // Coal generator
        this.shaped(Wormhole.coal_generator)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.ENDER_PEARLS)
            .input('C', Items.FURNACE)
            .input('D', ConventionalItemTags.REDSTONE_DUSTS)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);

        // Target definition device
        this.shaped(Wormhole.target_device)
            .pattern("  A")
            .pattern("BCB")
            .pattern("BDB")
            .input('A', ConventionalItemTags.REDSTONE_DUSTS)
            .input('B', ConventionalItemTags.IRON_INGOTS)
            .input('C', ConventionalItemTags.ENDER_PEARLS)
            .input('D', Items.COMPASS)
            .unlockedBy(ConventionalItemTags.IRON_INGOTS);

        // Advanced target definition device
        this.shaped(Wormhole.advanced_target_device)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("  A")
            .pattern("BCD")
            .pattern("BBB")
            .input('A', ConventionalItemTags.GLOWSTONE_DUSTS)
            .input('B', ConventionalItemTags.IRON_INGOTS)
            .input('C', Wormhole.target_device)
            .input('D', ConventionalItemTags.GOLD_INGOTS)
            .unlockedBy(Wormhole.target_device);
    }
}
