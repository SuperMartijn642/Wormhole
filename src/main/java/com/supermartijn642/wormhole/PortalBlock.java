package com.supermartijn642.wormhole;

import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock implements SimpleWaterloggedBlock {

    private static final BlockShape
        SHAPE_X = BlockShape.createBlockShape(6, 0, 0, 10, 16, 16),
        SHAPE_Y = BlockShape.createBlockShape(0, 6, 0, 16, 10, 16),
        SHAPE_Z = BlockShape.createBlockShape(0, 0, 6, 16, 16, 10);

    public static final EnumProperty<Direction.Axis> AXIS_PROPERTY = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.values());
    public static final EnumProperty<DyeColor> COLOR_PROPERTY = EnumProperty.create("color", DyeColor.class, DyeColor.values());
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PortalBlock(){
        super(BlockProperties.create().noCollision().destroyTime(-1.0F).explosionResistance(-1).sound(SoundType.GLASS).lightLevel(o -> 11).noLootTable(), () -> Wormhole.portal_tile);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS_PROPERTY, Direction.Axis.X).setValue(COLOR_PROPERTY, DyeColor.WHITE).setValue(WATERLOGGED, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof PortalBlockEntity)
            return ((PortalBlockEntity)entity).activate(player, hand) ? InteractionFeedback.SUCCESS : InteractionFeedback.PASS;
        return super.interact(state, level, pos, player, hand, hitSide, hitLocation);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof PortalBlockEntity)
            ((PortalBlockEntity)blockEntity).teleport(entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(AXIS_PROPERTY, COLOR_PROPERTY, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        Direction.Axis axis = state.getValue(AXIS_PROPERTY);
        return (axis == Direction.Axis.X ? SHAPE_X : axis == Direction.Axis.Y ? SHAPE_Y : axis == Direction.Axis.Z ? SHAPE_Z : BlockShape.empty()).getUnderlying();
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof PortalBlockEntity && !((PortalBlockEntity)entity).hasGroup())
            entity.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player){
        return ItemStack.EMPTY;
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos){
        if(state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type){
        return type == PathComputationType.WATER && level.getFluidState(pos).is(FluidTags.WATER);
    }
}
