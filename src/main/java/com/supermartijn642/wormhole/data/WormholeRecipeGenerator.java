package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.NBTRecipe;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

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
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.COBBLESTONE)
            .input('C', Tags.Items.OBSIDIAN)
            .unlockedBy(Tags.Items.INGOTS_IRON);

        // Portal stabilizer
        this.shaped(Wormhole.portal_stabilizer)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("AEA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.STORAGE_BLOCKS_LAPIS)
            .input('C', Tags.Items.DUSTS_GLOWSTONE)
            .input('D', Tags.Items.ENDER_PEARLS)
            .input('E', Tags.Items.STORAGE_BLOCKS_REDSTONE)
            .unlockedBy(Tags.Items.ENDER_PEARLS);

        // Basic energy cell
        this.shaped(Wormhole.basic_energy_cell)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.DUSTS_REDSTONE)
            .input('C', Tags.Items.STORAGE_BLOCKS_REDSTONE)
            .unlockedBy(Tags.Items.INGOTS_IRON);

        // Advanced energy cell
        this.shaped(Wormhole.advanced_energy_cell)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.GEMS_QUARTZ)
            .input('C', Items.REPEATER)
            .input('D', Wormhole.basic_energy_cell)
            .unlockedBy(Wormhole.basic_energy_cell);

        // Basic target cell
        this.shaped(Wormhole.basic_target_cell)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.GEMS_LAPIS)
            .input('C', Items.COMPASS)
            .unlockedBy(Tags.Items.INGOTS_IRON);

        // Advanced target cell
        this.shaped(Wormhole.advanced_target_cell)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.GEMS_LAPIS)
            .input('C', Tags.Items.DUSTS_GLOWSTONE)
            .input('D', Wormhole.basic_target_cell)
            .unlockedBy(Wormhole.basic_target_cell);

        // Coal generator
        this.shaped(Wormhole.coal_generator)
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.ENDER_PEARLS)
            .input('C', Items.FURNACE)
            .input('D', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy(Tags.Items.INGOTS_IRON);

        // Target definition device
        this.shaped(Wormhole.target_device)
            .pattern("  A")
            .pattern("BCB")
            .pattern("BDB")
            .input('A', Tags.Items.DUSTS_REDSTONE)
            .input('B', Tags.Items.INGOTS_IRON)
            .input('C', Tags.Items.ENDER_PEARLS)
            .input('D', Items.COMPASS)
            .unlockedBy(Tags.Items.INGOTS_IRON);

        // Advanced target definition device
        this.shaped(Wormhole.advanced_target_device)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("  A")
            .pattern("BCD")
            .pattern("BBB")
            .input('A', Tags.Items.DUSTS_GLOWSTONE)
            .input('B', Tags.Items.INGOTS_IRON)
            .input('C', Wormhole.target_device)
            .input('D', Tags.Items.INGOTS_GOLD)
            .unlockedBy(Wormhole.target_device);
    }
}
