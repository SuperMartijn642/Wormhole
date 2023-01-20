package com.supermartijn642.wormhole;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 2/8/2020 by SuperMartijn642
 */
public class NBTRecipe extends ShapedRecipe {

    public static final RecipeSerializer<NBTRecipe> SERIALIZER = new Serializer();

    private static final List<Item> VALID_ITEMS = new LinkedList<>();

    static{
        VALID_ITEMS.add(Wormhole.target_device);
        VALID_ITEMS.add(Wormhole.advanced_target_device);

        VALID_ITEMS.add(Item.byBlock(Wormhole.portal_stabilizer));
        VALID_ITEMS.add(Item.byBlock(Wormhole.basic_energy_cell));
        VALID_ITEMS.add(Item.byBlock(Wormhole.advanced_energy_cell));
        VALID_ITEMS.add(Item.byBlock(Wormhole.basic_target_cell));
        VALID_ITEMS.add(Item.byBlock(Wormhole.advanced_target_cell));
        VALID_ITEMS.add(Item.byBlock(Wormhole.coal_generator));
    }

    public NBTRecipe(ResourceLocation id, String group, CraftingBookCategory category, int recipeWidth, int recipeHeight, NonNullList<Ingredient> recipeItems, ItemStack recipeOutput){
        super(id, group, category, recipeWidth, recipeHeight, recipeItems, recipeOutput);
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory){
        CompoundTag compound = null;
        loop:
        for(int i = 0; i < inventory.getHeight(); i++){
            for(int j = 0; j < inventory.getWidth(); j++){
                ItemStack stack = inventory.getItem(i * inventory.getWidth() + j);
                if(stack.hasTag() && VALID_ITEMS.contains(stack.getItem())){
                    compound = stack.getTag();
                    break loop;
                }
            }
        }

        if(compound != null){
            ItemStack result = this.getResultItem().copy();
            result.getOrCreateTag().merge(compound);
            return result;
        }

        return this.getResultItem().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return super.getSerializer();
    }

    private static class Serializer implements RecipeSerializer<NBTRecipe> {

        @Override
        public NBTRecipe fromJson(ResourceLocation recipeId, JsonObject json){
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json);
            return new NBTRecipe(recipeId, recipe.getGroup(), recipe.category(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getResultItem());
        }

        @Nullable
        @Override
        public NBTRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer){
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer);
            return new NBTRecipe(recipeId, recipe.getGroup(), recipe.category(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getResultItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NBTRecipe recipe){
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
