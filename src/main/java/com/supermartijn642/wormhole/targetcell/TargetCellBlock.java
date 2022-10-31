package com.supermartijn642.wormhole.targetcell;

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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlock extends PortalGroupBlock {

    public static final PropertyInteger VISUAL_TARGETS;

    static{
        int maxCapacity = 0;
        for(TargetCellType type : TargetCellType.values()){
            if(type.getVisualCapacity() > maxCapacity)
                maxCapacity = type.getVisualCapacity();
        }
        VISUAL_TARGETS = PropertyInteger.create("targets", 0, maxCapacity);
    }

    private final TargetCellType type;

    public TargetCellBlock(TargetCellType type){
        super(type::getBlockEntityType);
        this.type = type;
        this.setDefaultState(this.getDefaultState().withProperty(VISUAL_TARGETS, 0));
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.target_cell.info").color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData", Constants.NBT.TAG_COMPOUND) ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int targets = tag == null || tag.hasNoTags() || !tag.hasKey("targetCount", Constants.NBT.TAG_INT) ? 0 : tag.getInteger("targetCount");
        int targetCapacity = this.type.getCapacity();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(TextFormatting.YELLOW).get());
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, VISUAL_TARGETS);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(VISUAL_TARGETS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(VISUAL_TARGETS, meta);
    }
}
