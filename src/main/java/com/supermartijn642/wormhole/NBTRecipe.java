package com.supermartijn642.wormhole;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 2/8/2020 by SuperMartijn642
 */
public class NBTRecipe extends ShapedRecipe {

    private static final MapCodec<NBTRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(instance, NBTRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,NBTRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC,
        recipe -> recipe.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
        recipe -> recipe.bookInfo,
        ShapedRecipePattern.STREAM_CODEC,
        recipe -> recipe.pattern,
        ItemStackTemplate.STREAM_CODEC,
        recipe -> recipe.result,
        NBTRecipe::new
    );
    public static final RecipeSerializer<NBTRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

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

    private final ShapedRecipePattern pattern;
    private final ItemStackTemplate result;

    public NBTRecipe(CommonInfo commonInfo, CraftingBookInfo craftingBookInfo, ShapedRecipePattern pattern, ItemStackTemplate result){
        super(commonInfo, craftingBookInfo, pattern, result);
        this.pattern = pattern;
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input){
        ItemStack result = this.result.create();
        loop:
        for(int i = 0; i < input.height(); i++){
            for(int j = 0; j < input.width(); j++){
                ItemStack stack = input.getItem(i * input.width() + j);
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
    public RecipeSerializer<ShapedRecipe> getSerializer(){
        //noinspection unchecked,rawtypes
        return (RecipeSerializer)SERIALIZER;
    }
}
