package com.supermartijn642.wormhole.targetcell;

import com.google.common.collect.ImmutableMap;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlock extends PortalGroupBlock {

    private final TargetCellType type;

    public TargetCellBlock(TargetCellType type){
        super(type.getRegistryName(), type::createTile);
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(new TranslatableComponent("wormhole.target_cell.info").withStyle(ChatFormatting.AQUA));

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            tooltip.add(new TranslatableComponent("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.INVISIBLE;
    }
}
