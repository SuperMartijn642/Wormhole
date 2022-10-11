package com.supermartijn642.wormhole;

import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock implements IWaterLoggable {

    private static final BlockShape
        SHAPE_X = BlockShape.createBlockShape(6, 0, 0, 10, 16, 16),
        SHAPE_Y = BlockShape.createBlockShape(0, 6, 0, 16, 10, 16),
        SHAPE_Z = BlockShape.createBlockShape(0, 0, 6, 16, 16, 10);

    public static final EnumProperty<Direction.Axis> AXIS_PROPERTY = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.values());
    public static final EnumProperty<DyeColor> COLOR_PROPERTY = EnumProperty.create("color", DyeColor.class, DyeColor.values());
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PortalBlock(){
        super(BlockProperties.create(Material.PORTAL).noCollision().destroyTime(-1.0F).explosionResistance(-1).sound(SoundType.GLASS).lightLevel(o -> 11).noLootTable(), () -> Wormhole.portal_tile);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS_PROPERTY, Direction.Axis.X).setValue(COLOR_PROPERTY, DyeColor.WHITE).setValue(WATERLOGGED, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof PortalBlockEntity)
            return ((PortalBlockEntity)entity).activate(player, hand) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return super.interact(state, level, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity){
        TileEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof PortalBlockEntity)
            ((PortalBlockEntity)blockEntity).teleport(entity);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(AXIS_PROPERTY, COLOR_PROPERTY, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context){
        Direction.Axis axis = state.getValue(AXIS_PROPERTY);
        return (axis == Direction.Axis.X ? SHAPE_X : axis == Direction.Axis.Y ? SHAPE_Y : axis == Direction.Axis.Z ? SHAPE_Z : BlockShape.empty()).getUnderlying();
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof PortalBlockEntity && !((PortalBlockEntity)entity).hasGroup())
            entity.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader level, BlockPos pos, PlayerEntity player){
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, IWorld level, BlockPos pos, BlockPos neighborPos){
        if(state.getValue(WATERLOGGED))
            level.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    public boolean isPathfindable(BlockState state, IBlockReader level, BlockPos pos, PathType type){
        return type == PathType.WATER && level.getFluidState(pos).is(FluidTags.WATER);
    }
}
