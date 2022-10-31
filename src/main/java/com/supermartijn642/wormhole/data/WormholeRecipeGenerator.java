package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.NBTRecipe;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

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
        this.shaped(Wormhole.portal_frame.asItem(), 2)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "cobblestone")
            .input('C', "obsidian")
            .unlockedByOreDict("ingotIron");

        // Portal stabilizer
        this.shaped(Wormhole.portal_stabilizer.asItem())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("AEA")
            .input('A', "ingotIron")
            .input('B', "blockLapis")
            .input('C', "dustGlowstone")
            .input('D', "enderpearl")
            .input('E', "blockRedstone")
            .unlockedByOreDict("enderpearl");

        // Basic energy cell
        this.shaped(Wormhole.basic_energy_cell.asItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "dustRedstone")
            .input('C', "blockRedstone")
            .unlockedByOreDict("ingotIron");

        // Advanced energy cell
        this.shaped(Wormhole.advanced_energy_cell.asItem())
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "gemQuartz")
            .input('C', Items.REPEATER)
            .input('D', Wormhole.basic_energy_cell.asItem())
            .unlockedBy(Wormhole.basic_energy_cell.asItem());

        // Basic target cell
        this.shaped(Wormhole.basic_target_cell.asItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "gemLapis")
            .input('C', Items.COMPASS)
            .unlockedByOreDict("ingotIron");

        // Advanced target cell
        this.shaped(Wormhole.advanced_target_cell.asItem())
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', "ingotIron")
            .input('B', "gemLapis")
            .input('C', "dustGlowstone")
            .input('D', Wormhole.basic_target_cell.asItem())
            .unlockedBy(Wormhole.basic_target_cell.asItem());

        // Coal generator
        this.shaped(Wormhole.coal_generator.asItem())
            .pattern("ABA")
            .pattern("ACA")
            .pattern("ADA")
            .input('A', "ingotIron")
            .input('B', "enderpearl")
            .input('C', Item.getItemFromBlock(Blocks.FURNACE))
            .input('D', "dustRedstone")
            .unlockedByOreDict("ingotIron");

        // Target definition device
        this.shaped(Wormhole.target_device)
            .pattern("  A")
            .pattern("BCB")
            .pattern("BDB")
            .input('A', "dustRedstone")
            .input('B', "ingotIron")
            .input('C', "enderpearl")
            .input('D', Items.COMPASS)
            .unlockedByOreDict("ingotIron");

        // Advanced target definition device
        this.shaped(Wormhole.advanced_target_device)
            .customSerializer(NBTRecipe.SERIALIZER)
            .pattern("  A")
            .pattern("BCD")
            .pattern("BBB")
            .input('A', "dustGlowstone")
            .input('B', "ingotIron")
            .input('C', Wormhole.target_device)
            .input('D', "ingotGold")
            .unlockedBy(Wormhole.target_device);
    }
}
