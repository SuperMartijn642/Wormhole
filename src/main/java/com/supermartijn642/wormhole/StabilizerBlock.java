package com.supermartijn642.wormhole;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

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
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof StabilizerBlockEntity)
            return ((StabilizerBlockEntity)entity).activate(player) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return InteractionFeedback.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(ON_PROPERTY);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.portal_stabilizer.info").color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = WormholeConfig.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            info.accept(TextComponents.translation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).color(ChatFormatting.YELLOW).get());

        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int energyCapacity = WormholeConfig.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            info.accept(TextComponents.translation(EnergyFormat.formatCapacityWithUnit(energy, energyCapacity)).color(ChatFormatting.YELLOW).get());
    }
}
