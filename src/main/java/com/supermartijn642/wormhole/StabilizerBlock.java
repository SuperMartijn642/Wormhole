package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final BooleanProperty ON_PROPERTY = BooleanProperty.create("on");

    public StabilizerBlock(){
        super("portal_stabilizer", StabilizerTile::new);
        this.setDefaultState(this.getDefaultState().with(ON_PROPERTY, false));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof StabilizerTile)
            return ((StabilizerTile)tile).activate(player);
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ON_PROPERTY);
    }

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TranslationTextComponent("wormhole.portal_stabilizer.info").applyTextStyle(TextFormatting.AQUA));

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;

        int targets = tag == null || tag.isEmpty() || !tag.contains("targetCount") ? 0 : tag.getInt("targetCount");
        int targetCapacity = WormholeConfig.INSTANCE.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            tooltip.add(new TranslationTextComponent("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).applyTextStyle(TextFormatting.YELLOW));

        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        int energyCapacity = WormholeConfig.INSTANCE.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            tooltip.add(new StringTextComponent(EnergyFormat.formatCapacity(energy, energyCapacity)).applyTextStyle(TextFormatting.YELLOW));
    }
}
