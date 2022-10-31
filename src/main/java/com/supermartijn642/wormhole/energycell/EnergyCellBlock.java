package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlock extends PortalGroupBlock {

    public static final PropertyInteger ENERGY_LEVEL = PropertyInteger.create("energy_level", 0, 15);

    private final EnergyCellType type;

    public EnergyCellBlock(EnergyCellType type){
        super(type::getBlockEntityType);
        this.type = type;
        this.setDefaultState(this.getDefaultState().withProperty(ENERGY_LEVEL, 0));
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.energy_cell.info").color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData") ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.hasNoTags() || !tag.hasKey("energy") ? 0 : tag.getInteger("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, capacity)).color(TextFormatting.YELLOW).get());
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ENERGY_LEVEL);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ENERGY_LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ENERGY_LEVEL, meta);
    }
}
