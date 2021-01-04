package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlock extends PortalGroupBlock {

    private final EnergyCellType type;

    public EnergyCellBlock(EnergyCellType type){
        super(type.getRegistryName(), type::createTile);
        this.type = type;
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TranslationTextComponent("wormhole.energy_cell.info").applyTextStyle(TextFormatting.AQUA));

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            tooltip.add(new StringTextComponent(EnergyFormat.formatCapacity(energy, capacity)).applyTextStyle(TextFormatting.YELLOW));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return this.type == EnergyCellType.CREATIVE ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }
}
