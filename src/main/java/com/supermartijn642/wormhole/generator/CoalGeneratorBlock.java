package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeBlock;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlock extends WormholeBlock implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Shapes.box(2 / 16d, 0, 1 / 16d, 14 / 16d, 12 / 16d, 13 / 16d),
        Shapes.box(3 / 16d, 0, 13 / 16d, 7 / 16d, 7 / 16d, 15 / 16d),
        Shapes.box(4 / 16d, 7 / 16d, 13 / 16d, 6 / 16d, 10 / 16d, 14 / 16d),
        Shapes.box(9 / 16d, 0, 13 / 16d, 13 / 16d, 7 / 16d, 15 / 16d),
        Shapes.box(10 / 16d, 7 / 16d, 13 / 16d, 12 / 16d, 10 / 16d, 14 / 16d)
    );
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static{
        SHAPES[Direction.NORTH.get2DDataValue()] = SHAPE;
        SHAPES[Direction.EAST.get2DDataValue()] = rotateShape(Direction.NORTH, Direction.EAST, SHAPE);
        SHAPES[Direction.SOUTH.get2DDataValue()] = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE);
        SHAPES[Direction.WEST.get2DDataValue()] = rotateShape(Direction.NORTH, Direction.WEST, SHAPE);
    }

    /**
     * Credits to wyn_price
     * @see <a href="https://forums.minecraftforge.net/topic/74979-1144-rotate-voxel-shapes/?do=findComment&comment=391969">Minecraft Forge forum post</a>
     */
    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape){
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for(int i = 0; i < times; i++){
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public CoalGeneratorBlock(){
        super("coal_generator", true, Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectToolForDrops());
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit){
        if(!worldIn.isClientSide)
            NetworkHooks.openScreen((ServerPlayer)player, new MenuProvider() {
                @Override
                public Component getDisplayName(){
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player){
                    return new CoalGeneratorContainer(windowId, player, pos);
                }
            }, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        int range = 2 * WormholeConfig.coalGeneratorRange.get() + 1;
        tooltip.add(Component.translatable("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower.get())).withStyle(ChatFormatting.AQUA));

        CompoundTag tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;
        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        tooltip.add(Component.literal(EnergyFormat.formatCapacity(energy, WormholeConfig.coalGeneratorCapacity.get())).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos){
        return state.getValue(LIT) ? 8 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new CoalGeneratorTile(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(LIT, FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> blockEntityType){
        return blockEntityType == Wormhole.coal_generator_tile ?
            (world2, pos, state2, entity) -> ((GeneratorTile)entity).tick() : null;
    }
}
