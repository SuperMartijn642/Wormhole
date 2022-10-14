package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class EnergyCellBlock extends PortalGroupBlock {

    public static final IntegerProperty ENERGY_LEVEL = IntegerProperty.create("energy_level", 0, 15);

    private final EnergyCellType type;

    public EnergyCellBlock(EnergyCellType type){
        super(type::getBlockEntityType);
        this.type = type;
        this.registerDefaultState(this.defaultBlockState().setValue(ENERGY_LEVEL, 0));
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.energy_cell.info").color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, capacity)).color(TextFormatting.YELLOW).get());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ENERGY_LEVEL);
    }
}
