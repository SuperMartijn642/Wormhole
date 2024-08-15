package com.supermartijn642.wormhole.energycell;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

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
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.energy_cell.info").color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.get(BaseBlock.TILE_DATA);

        int energy = this.type == EnergyCellType.CREATIVE ? this.type.getCapacity() :
            tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int capacity = this.type.getCapacity();

        if(capacity > 0)
            info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, capacity)).color(ChatFormatting.YELLOW).get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(ENERGY_LEVEL);
    }
}
