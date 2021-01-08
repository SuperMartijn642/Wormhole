package com.supermartijn642.wormhole;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 2/8/2020 by SuperMartijn642
 */
public class NBTRecipe extends ShapedRecipe {

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

    public NBTRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn){
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv){
        CompoundNBT compound = null;
        loop:
        for(int i = 0; i < inv.getHeight(); i++){
            for(int j = 0; j < inv.getWidth(); j++){
                ItemStack stack = inv.getStackInSlot(i * inv.getWidth() + j);
                if(stack.hasTag() && VALID_ITEMS.contains(stack.getItem())){
                    compound = stack.getTag();
                    break loop;
                }
            }
        }

        if(compound != null){
            ItemStack result = this.getRecipeOutput().copy();
            result.getOrCreateTag().merge(compound);
            return result;
        }

        return this.getRecipeOutput().copy();
    }

    @Override
    public IRecipeSerializer<?> getSerializer(){
        return super.getSerializer();
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NBTRecipe> {
        @Override
        public NBTRecipe read(ResourceLocation recipeId, JsonObject json){
            ShapedRecipe recipe = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json);
            return new NBTRecipe(recipeId, recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getRecipeOutput());
        }

        @Nullable
        @Override
        public NBTRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
            ShapedRecipe recipe = IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer);
            return new NBTRecipe(recipeId, recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getRecipeOutput());
        }

        @Override
        public void write(PacketBuffer buffer, NBTRecipe recipe){
            IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
        }
    }
}
