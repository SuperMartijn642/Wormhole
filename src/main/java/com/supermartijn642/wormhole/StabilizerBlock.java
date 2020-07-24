package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

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
            return ((StabilizerTile)tile).activate(player, handIn);
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ON_PROPERTY);
    }
}
