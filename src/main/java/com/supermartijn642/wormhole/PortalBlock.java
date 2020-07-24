package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlock extends PortalGroupBlock {

    private static final VoxelShape
        SHAPE_X = VoxelShapes.create(6 / 16d, 0, 0, 10 / 16d, 1, 1),
        SHAPE_Y = VoxelShapes.create(0, 6 / 16d, 0, 1, 10 / 16d, 1),
        SHAPE_Z = VoxelShapes.create(0, 0, 6 / 16d, 1, 1, 10 / 16d);

    public static final EnumProperty<Direction.Axis> AXIS_PROPERTY = EnumProperty.create("axis", Direction.Axis.class, Direction.Axis.values());
    public static final EnumProperty<DyeColor> COLOR_PROPERTY = EnumProperty.create("color", DyeColor.class, DyeColor.values());

    public PortalBlock(){
        super(Properties.create(Material.PORTAL).doesNotBlockMovement().hardnessAndResistance(-1.0F).sound(SoundType.GLASS).lightValue(11).noDrops(), "portal", PortalTile::new);
        this.setDefaultState(this.getDefaultState().with(AXIS_PROPERTY, Direction.Axis.X).with(COLOR_PROPERTY, DyeColor.WHITE));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PortalTile)
            return ((PortalTile)tile).activate(player, handIn) ? ActionResultType.SUCCESS : ActionResultType.PASS;
        return super.onBlockActivated(state, worldIn, pos, player, handIn, p_225533_6_);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PortalTile)
            ((PortalTile)tile).teleport(entityIn);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(AXIS_PROPERTY, COLOR_PROPERTY);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        Direction.Axis axis = state.get(AXIS_PROPERTY);
        return axis == Direction.Axis.X ? SHAPE_X : axis == Direction.Axis.Y ? SHAPE_Y : axis == Direction.Axis.Z ? SHAPE_Z : VoxelShapes.empty();
    }
}
