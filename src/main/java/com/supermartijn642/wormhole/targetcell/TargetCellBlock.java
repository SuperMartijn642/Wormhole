package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

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
        tooltip.add(Component.translatable("wormhole.target_cell.info").withStyle(ChatFormatting.AQUA));

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            tooltip.add(Component.translatable("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.INVISIBLE;
    }
}
