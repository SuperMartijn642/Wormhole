package com.supermartijn642.wormhole;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 2/8/2020 by SuperMartijn642
 */
public class NBTRecipeFactory implements IRecipeFactory {

    public static final List<Item> VALID_ITEMS = new LinkedList<>();

    static{
        VALID_ITEMS.add(Wormhole.target_device);
        VALID_ITEMS.add(Wormhole.advanced_target_device);

        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.portal_stabilizer));
        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.basic_energy_cell));
        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.advanced_energy_cell));
        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.basic_target_cell));
        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.advanced_target_cell));
        VALID_ITEMS.add(Item.getItemFromBlock(Wormhole.coal_generator));
    }

    @Override
    public IRecipe parse(JsonContext context, JsonObject json){
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
        primer.height = recipe.getRecipeHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();
        return new NBTRecipe(new ResourceLocation(Wormhole.MODID, "nbtrecipe"), recipe.getRecipeOutput(), primer);
    }

    public static class NBTRecipe extends ShapedOreRecipe {

        public NBTRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer){
            super(group, result, primer);
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv){
            NBTTagCompound compound = null;
            loop:
            for(int i = 0; i < inv.getHeight(); i++){
                for(int j = 0; j < inv.getWidth(); j++){
                    ItemStack stack = inv.getStackInSlot(i * inv.getWidth() + j);
                    if(stack.hasTagCompound() && VALID_ITEMS.contains(stack.getItem())){
                        compound = stack.getTagCompound();
                        break loop;
                    }
                }
            }

            if(compound != null){
                ItemStack result = this.getRecipeOutput().copy();
                NBTTagCompound compound1 = result.hasTagCompound() ? result.getTagCompound() : new NBTTagCompound();
                compound1.merge(compound);
                result.setTagCompound(compound);
                return result;
            }

            return this.getRecipeOutput().copy();
        }
    }
}
