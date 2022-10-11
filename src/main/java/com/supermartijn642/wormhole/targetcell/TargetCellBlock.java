package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlock extends PortalGroupBlock {

    public static final IntegerProperty VISUAL_TARGETS;

    static{
        int maxCapacity = 0;
        for(TargetCellType type : TargetCellType.values()){
            if(type.getVisualCapacity() > maxCapacity)
                maxCapacity = type.getVisualCapacity();
        }
        VISUAL_TARGETS = IntegerProperty.create("targets", 0, maxCapacity);
    }

    private final TargetCellType type;

    public TargetCellBlock(TargetCellType type){
        super(type::getBlockEntityType);
        this.type = type;
        this.registerDefaultState(this.defaultBlockState().setValue(VISUAL_TARGETS, 0));
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.target_cell.info").color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.hasTag() && stack.getTag().contains("tileData", Tag.TAG_COMPOUND) ? stack.getTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount", Tag.TAG_INT) ? 0 : tag.getInt("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(ChatFormatting.YELLOW).get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(VISUAL_TARGETS);
    }
}
