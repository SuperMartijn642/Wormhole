package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock {

    private static final VoxelShape
        SHAPE_X = Shapes.box(6 / 16d, 0, 0, 10 / 16d, 1, 1),
        SHAPE_Y = Shapes.box(0, 6 / 16d, 0, 1, 10 / 16d, 1),
        SHAPE_Z = Shapes.box(0, 0, 6 / 16d, 1, 1, 10 / 16d);

    public static final EnumProperty<Direction.Axis> AXIS_PROPERTY = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.values());
    public static final EnumProperty<DyeColor> COLOR_PROPERTY = EnumProperty.create("color", DyeColor.class, DyeColor.values());

    public PortalBlock(){
        super(Properties.of(Material.PORTAL).noCollission().strength(-1.0F).sound(SoundType.GLASS).lightLevel(o -> 11).noDrops(), "portal", PortalTile::new);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS_PROPERTY, Direction.Axis.X).setValue(COLOR_PROPERTY, DyeColor.WHITE));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof PortalTile)
            return ((PortalTile)tile).activate(player, handIn) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        return super.use(state, worldIn, pos, player, handIn, p_225533_6_);
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn){
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof PortalTile)
            ((PortalTile)tile).teleport(entityIn);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(AXIS_PROPERTY, COLOR_PROPERTY);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
        Direction.Axis axis = state.getValue(AXIS_PROPERTY);
        return axis == Direction.Axis.X ? SHAPE_X : axis == Direction.Axis.Y ? SHAPE_Y : axis == Direction.Axis.Z ? SHAPE_Z : Shapes.empty();
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor){
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof PortalTile && !((PortalTile)tile).hasGroup())
            tile.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player){
        return ItemStack.EMPTY;
    }
}
