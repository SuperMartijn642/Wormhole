package com.supermartijn642.wormhole;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock {

    private static final AxisAlignedBB
        SHAPE_X = new AxisAlignedBB(6 / 16d, 0, 0, 10 / 16d, 1, 1),
        SHAPE_Y = new AxisAlignedBB(0, 6 / 16d, 0, 1, 10 / 16d, 1),
        SHAPE_Z = new AxisAlignedBB(0, 0, 6 / 16d, 1, 1, 10 / 16d);

    public static final PropertyEnum<EnumDyeColor> COLOR_PROPERTY = PropertyEnum.create("color", EnumDyeColor.class, EnumDyeColor.values());

    public final EnumFacing.Axis axis;

    public PortalBlock(EnumFacing.Axis axis){
        super(Material.PORTAL, Material.PORTAL.getMaterialMapColor(), "portal_" + axis.name().toLowerCase(Locale.ROOT), PortalTile::new);
        this.axis = axis;
        this.setDefaultState(this.blockState.getBaseState().withProperty(COLOR_PROPERTY, EnumDyeColor.WHITE));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PortalTile)
            return ((PortalTile)tile).activate(player, hand);
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PortalTile)
            ((PortalTile)tile).teleport(entityIn);
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, COLOR_PROPERTY);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return this.axis == EnumFacing.Axis.X ? SHAPE_X : this.axis == EnumFacing.Axis.Y ? SHAPE_Y : this.axis == EnumFacing.Axis.Z ? SHAPE_Z : NULL_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
        return NULL_AABB;
    }

    public boolean isFullCube(IBlockState state){
        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer(){
        return BlockRenderLayer.TRANSLUCENT;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(COLOR_PROPERTY, EnumDyeColor.byDyeDamage(meta & 15));
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(COLOR_PROPERTY).getDyeDamage();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }
}
