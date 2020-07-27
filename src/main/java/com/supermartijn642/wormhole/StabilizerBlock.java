package com.supermartijn642.wormhole;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final PropertyBool ON_PROPERTY = PropertyBool.create("on");

    public StabilizerBlock(){
        super("portal_stabilizer", StabilizerTile::new);
        this.setDefaultState(this.getDefaultState().withProperty(ON_PROPERTY, false));

        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof StabilizerTile)
            return ((StabilizerTile)tile).activate(player, hand);
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ON_PROPERTY);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ON_PROPERTY) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ON_PROPERTY, meta == 1);
    }
}
