package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlock extends PortalGroupBlock {

    public static final PropertyInteger STUPID_MODEL_LOADING_SOLUTION = PropertyInteger.create("model", 0, 16);

    private final EnergyCellType type;

    public EnergyCellBlock(EnergyCellType type){
        super(type.getRegistryName(), type::createTile);
        this.type = type;

        this.setDefaultState(this.getDefaultState().withProperty(STUPID_MODEL_LOADING_SOLUTION, 0));
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TextComponentTranslation("wormhole.energy_cell.info").setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData") ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.hasNoTags() || !tag.hasKey("energy") ? 0 : tag.getInteger("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            tooltip.add(new TextComponentString(EnergyFormat.formatCapacity(energy, capacity)).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return this.type == EnergyCellType.CREATIVE ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
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
