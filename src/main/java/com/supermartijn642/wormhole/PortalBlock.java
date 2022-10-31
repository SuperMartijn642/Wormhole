package com.supermartijn642.wormhole;

import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock {

    private static final BlockShape
        SHAPE_X = BlockShape.createBlockShape(6, 0, 0, 10, 16, 16),
        SHAPE_Y = BlockShape.createBlockShape(0, 6, 0, 16, 10, 16),
        SHAPE_Z = BlockShape.createBlockShape(0, 0, 6, 16, 16, 10);

    public static final PropertyEnum<EnumDyeColor> COLOR_PROPERTY = PropertyEnum.create("color", EnumDyeColor.class, EnumDyeColor.values());

    public final EnumFacing.Axis axis;

    public PortalBlock(EnumFacing.Axis axis){
        super(BlockProperties.create(Material.PORTAL).noCollision().destroyTime(-1.0F).explosionResistance(-1).sound(SoundType.GLASS).lightLevel(o -> 11).noLootTable(), () -> Wormhole.portal_tile);
        this.axis = axis;
        this.setDefaultState(this.getDefaultState().withProperty(COLOR_PROPERTY, EnumDyeColor.WHITE));
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof PortalBlockEntity)
            return ((PortalBlockEntity)entity).activate(player, hand) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return super.interact(state, level, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    public void onEntityCollidedWithBlock(World level, BlockPos pos, IBlockState state, Entity entity){
        TileEntity blockEntity = level.getTileEntity(pos);
        if(blockEntity instanceof PortalBlockEntity)
            ((PortalBlockEntity)blockEntity).teleport(entity);
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, COLOR_PROPERTY);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess level, BlockPos pos){
        return (this.axis == EnumFacing.Axis.X ? SHAPE_X : this.axis == EnumFacing.Axis.Y ? SHAPE_Y : this.axis == EnumFacing.Axis.Z ? SHAPE_Z : BlockShape.empty()).simplify();
    }

    @Override
    public void onNeighborChange(IBlockAccess level, BlockPos pos, BlockPos neighbor){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof PortalBlockEntity && !((PortalBlockEntity)entity).hasGroup())
            entity.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World level, BlockPos pos, EntityPlayer player){
        return ItemStack.EMPTY;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess level, IBlockState state, BlockPos pos, EnumFacing face){
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        int color = meta & 15;
        return this.getDefaultState().withProperty(COLOR_PROPERTY, EnumDyeColor.byDyeDamage(color));
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
