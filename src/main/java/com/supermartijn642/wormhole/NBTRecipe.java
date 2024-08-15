package com.supermartijn642.wormhole;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

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
    public ItemStack assemble(CraftingContainer inventory, HolderLookup.Provider provider){
        ItemStack result = this.getResultItem(provider).copy();
        loop:
        for(int i = 0; i < inventory.getHeight(); i++){
            for(int j = 0; j < inventory.getWidth(); j++){
                ItemStack stack = inventory.getItem(i * inventory.getWidth() + j);
                if(!VALID_ITEMS.contains(stack.getItem()))
                    continue;
                if(stack.has(BaseBlock.TILE_DATA)){
                    result.set(BaseBlock.TILE_DATA, stack.get(BaseBlock.TILE_DATA));
                    break loop;
                }
                if(stack.has(TargetDeviceItem.TARGETS)){
                    result.set(TargetDeviceItem.TARGETS, stack.get(TargetDeviceItem.TARGETS));
                    break loop;
                }
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return super.getSerializer();
    }

    private static class Serializer implements RecipeSerializer<NBTRecipe> {

        private static final MapCodec<NBTRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(recipe -> recipe.showNotification)
            ).apply(instance, NBTRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,NBTRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(
            Serializer::fromShapedRecipe,
            Function.identity()
        );

        @Override
        public MapCodec<NBTRecipe> codec(){
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf,NBTRecipe> streamCodec(){
            return STREAM_CODEC;
        }

        private static NBTRecipe fromShapedRecipe(ShapedRecipe recipe){
            return new NBTRecipe(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(null), recipe.showNotification());
        }
    }
}
