package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlock extends PortalGroupBlock {

    private static final PropertyInteger STUPID_MODEL_LOADING_SOLUTION = PropertyInteger.create("model", 0, 9);

    private final TargetCellType type;

    public TargetCellBlock(TargetCellType type){
        super(type.getRegistryName(), type::createTile);
        this.type = type;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TextComponentTranslation("wormhole.target_cell.info").setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData") ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int targets = tag == null || tag.hasNoTags() || !tag.hasKey("targetCount") ? 0 : tag.getInteger("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            tooltip.add(new TextComponentTranslation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, STUPID_MODEL_LOADING_SOLUTION);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return 0;
    }
}
