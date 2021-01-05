package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

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
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TranslationTextComponent("wormhole.target_cell.info").applyTextStyle(TextFormatting.AQUA));

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            tooltip.add(new TranslationTextComponent("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).applyTextStyle(TextFormatting.YELLOW));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.INVISIBLE;
    }
}
