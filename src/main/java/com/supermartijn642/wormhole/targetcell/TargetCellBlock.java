package com.supermartijn642.wormhole.targetcell;

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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
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
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.target_cell.info").color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.hasTag() && stack.getTag().contains("tileData", Constants.NBT.TAG_COMPOUND) ? stack.getTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount", Constants.NBT.TAG_INT) ? 0 : tag.getInt("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(TextFormatting.YELLOW).get());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(VISUAL_TARGETS);
    }
}
