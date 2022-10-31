package com.supermartijn642.wormhole;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final PropertyBool ON_PROPERTY = PropertyBool.create("on");

    public StabilizerBlock(){
        super(() -> Wormhole.stabilizer_tile);
        this.setDefaultState(this.getDefaultState().withProperty(ON_PROPERTY, false));
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof StabilizerBlockEntity)
            return ((StabilizerBlockEntity)entity).activate(player) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return InteractionFeedback.PASS;
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ON_PROPERTY);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.portal_stabilizer.info").color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData") ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int targets = tag == null || tag.hasNoTags() || !tag.hasKey("targetCount") ? 0 : tag.getInteger("targetCount");
        int targetCapacity = WormholeConfig.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(TextFormatting.YELLOW).get());

        int energy = tag == null || tag.hasNoTags() || !tag.hasKey("energy") ? 0 : tag.getInteger("energy");
        int energyCapacity = WormholeConfig.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            info.accept(TextComponents.translation(EnergyFormat.formatCapacityWithUnit(energy, energyCapacity)).color(TextFormatting.YELLOW).get());
    }

    public int getMetaFromState(IBlockState state){
        return state.getValue(ON_PROPERTY) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ON_PROPERTY, meta == 1);
    }
}
