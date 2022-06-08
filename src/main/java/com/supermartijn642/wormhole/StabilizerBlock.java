package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final BooleanProperty ON_PROPERTY = BooleanProperty.create("on");

    public StabilizerBlock(){
        super("portal_stabilizer", StabilizerTile::new);
        this.registerDefaultState(this.defaultBlockState().setValue(ON_PROPERTY, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof StabilizerTile)
            return ((StabilizerTile)tile).activate(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(ON_PROPERTY);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(Component.translatable("wormhole.portal_stabilizer.info").withStyle(ChatFormatting.AQUA));

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = WormholeConfig.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            tooltip.add(Component.translatable("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).withStyle(ChatFormatting.YELLOW));

        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int energyCapacity = WormholeConfig.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            tooltip.add(Component.literal(EnergyFormat.formatCapacity(energy, energyCapacity)).withStyle(ChatFormatting.YELLOW));
    }
}
