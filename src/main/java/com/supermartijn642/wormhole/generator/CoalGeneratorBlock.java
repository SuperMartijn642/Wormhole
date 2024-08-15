package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Consumer;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlock extends BaseBlock implements EntityHoldingBlock {

    private static final BlockShape SHAPE = BlockShape.or(
        BlockShape.createBlockShape(2, 0, 1, 14, 12, 13),
        BlockShape.createBlockShape(3, 0, 13, 7, 7, 15),
        BlockShape.createBlockShape(4, 7, 13, 6, 10, 14),
        BlockShape.createBlockShape(9, 0, 13, 13, 7, 15),
        BlockShape.createBlockShape(10, 7, 13, 12, 10, 14)
    );
    private static final BlockShape[] SHAPES = new BlockShape[4];

    static{
        SHAPES[Direction.NORTH.get2DDataValue()] = SHAPE;
        SHAPES[Direction.EAST.get2DDataValue()] = SHAPE.rotate(Direction.Axis.Y);
        SHAPES[Direction.SOUTH.get2DDataValue()] = SHAPE.rotate(Direction.Axis.Y).rotate(Direction.Axis.Y);
        SHAPES[Direction.WEST.get2DDataValue()] = SHAPE.rotate(Direction.Axis.Y).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y);
    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CoalGeneratorBlock(){
        super(true, BlockProperties.create().mapColor(MapColor.COLOR_GRAY).sound(SoundType.METAL).lightLevel(state -> state.getValue(LIT) ? 8 : 0).requiresCorrectTool().destroyTime(1.2f).explosionResistance(6));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(!level.isClientSide)
            CommonUtils.openContainer(new CoalGeneratorContainer(player, pos));
        return InteractionFeedback.CONSUME;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        int range = 2 * WormholeConfig.coalGeneratorRange.get() + 1;
        info.accept(TextComponents.translation("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower.get())).color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.get(BaseBlock.TILE_DATA);
        int energy = tag == null || tag.isEmpty() || !tag.contains("energy", Tag.TAG_INT) ? 0 : tag.getInt("energy");
        info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, WormholeConfig.coalGeneratorCapacity.get())).color(ChatFormatting.YELLOW).get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPES[state.getValue(FACING).get2DDataValue()].getUnderlying();
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return Wormhole.coal_generator_tile.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(LIT, FACING);
    }
}
