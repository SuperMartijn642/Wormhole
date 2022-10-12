package com.supermartijn642.wormhole;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final BooleanProperty ON_PROPERTY = BooleanProperty.create("on");

    public StabilizerBlock(){
        super(() -> Wormhole.stabilizer_tile);
        this.registerDefaultState(this.defaultBlockState().setValue(ON_PROPERTY, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof StabilizerBlockEntity)
            return ((StabilizerBlockEntity)entity).activate(player) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return InteractionFeedback.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ON_PROPERTY);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.portal_stabilizer.info").color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = WormholeConfig.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(TextFormatting.YELLOW).get());

        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int energyCapacity = WormholeConfig.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            info.accept(TextComponents.translation(EnergyFormat.formatCapacityWithUnit(energy, energyCapacity)).color(TextFormatting.YELLOW).get());
    }
}
