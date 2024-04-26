package com.supermartijn642.wormhole;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

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

    private final String group;
    private final CraftingBookCategory category;
    private final ShapedRecipePattern pattern;
    private final ItemStack result;
    private final boolean showNotification;

    public NBTRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack recipeOutput, boolean showNotification){
        super(group, category, pattern, recipeOutput, showNotification);
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.result = recipeOutput;
        this.showNotification = showNotification;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registryAccess){
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
            ItemStack result = this.getResultItem(registryAccess).copy();
            result.getOrCreateTag().merge(compound);
            return result;
        }

        return this.getResultItem(registryAccess).copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return super.getSerializer();
    }

    private static class Serializer implements RecipeSerializer<NBTRecipe> {

        private static final Codec<NBTRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(recipe -> recipe.group),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(recipe -> recipe.showNotification)
            ).apply(instance, NBTRecipe::new));

        @Override
        public Codec<NBTRecipe> codec(){
            return CODEC;
        }

        @Override
        public NBTRecipe fromNetwork(FriendlyByteBuf buffer){
            //noinspection DataFlowIssue
            return fromShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NBTRecipe recipe){
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }

        private static NBTRecipe fromShapedRecipe(ShapedRecipe recipe){
            return new NBTRecipe(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(null), recipe.showNotification());
        }
    }
}
