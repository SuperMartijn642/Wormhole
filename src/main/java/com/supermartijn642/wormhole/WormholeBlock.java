package com.supermartijn642.wormhole;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class WormholeBlock extends Block {

    private final boolean saveTileData;

    public WormholeBlock(String registryName, boolean saveTileData, Properties properties){
        super(properties);
        this.setRegistryName(registryName);
        this.saveTileData = saveTileData;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        CompoundTag tag = stack.getTag();
        tag = tag == null ? null : tag.contains("tileData") ? tag.getCompound("tileData") : null;
        if(tag == null || tag.isEmpty())
            return;

        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof WormholeTile)
            ((WormholeTile)tile).readData(tag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder){
        List<ItemStack> items = super.getDrops(state, builder);

        if(!this.saveTileData)
            return items;

        BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(!(tile instanceof WormholeTile))
            return items;

        CompoundTag tileTag = ((WormholeTile)tile).writeData();
        if(tileTag == null || tileTag.isEmpty())
            return items;

        CompoundTag tag = new CompoundTag();
        tag.put("tileData", tileTag);

        for(ItemStack stack : items){
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this){
                stack.setTag(tag);
            }
        }

        return items;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player){
        ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        BlockEntity tile = world.getBlockEntity(pos);
        if(!(tile instanceof WormholeTile))
            return stack;

        CompoundTag tileTag = ((WormholeTile)tile).writeData();
        if(tileTag == null || tileTag.isEmpty())
            return stack;

        CompoundTag tag = new CompoundTag();
        tag.put("tileData", tileTag);

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this)
            stack.setTag(tag);

        return stack;
    }
}
