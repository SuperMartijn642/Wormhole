package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.EnergyFormat;
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
public class EnergyCellBlock extends PortalGroupBlock {

    private final EnergyCellType type;

    public EnergyCellBlock(EnergyCellType type){
        super(type.getRegistryName(), type::createTile);
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(Component.translatable("wormhole.energy_cell.info").withStyle(ChatFormatting.AQUA));

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            tooltip.add(Component.literal(EnergyFormat.formatCapacity(energy, capacity)).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return this.type == EnergyCellType.CREATIVE ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }
}
