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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
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
        super(true, BlockProperties.create(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectTool().destroyTime(1.2f).explosionResistance(6));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        if(!level.isClientSide)
            CommonUtils.openContainer(new CoalGeneratorContainer(player, pos));
        return InteractionFeedback.CONSUME;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        int range = 2 * WormholeConfig.coalGeneratorRange.get() + 1;
        info.accept(TextComponents.translation("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower.get())).color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.hasTag() && stack.getTag().contains("tileData", Constants.NBT.TAG_COMPOUND) ? stack.getTag().getCompound("tileData") : null;
        int energy = tag == null || tag.isEmpty() || !tag.contains("energy", Constants.NBT.TAG_INT) ? 0 : tag.getInt("energy");
        info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, WormholeConfig.coalGeneratorCapacity.get())).color(TextFormatting.YELLOW).get());
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context){
        return SHAPES[state.getValue(FACING).get2DDataValue()].getUnderlying();
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader level, BlockPos pos){
        return state.getValue(LIT) ? 8 : 0;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return Wormhole.coal_generator_tile.create();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(LIT, FACING);
    }
}
