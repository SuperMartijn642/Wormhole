package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.WormholeBlock;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlock extends WormholeBlock {

    private static final VoxelShape SHAPE = VoxelShapes.or(
        VoxelShapes.box(2 / 16d, 0, 1 / 16d, 14 / 16d, 12 / 16d, 13 / 16d),
        VoxelShapes.box(3 / 16d, 0, 13 / 16d, 7 / 16d, 7 / 16d, 15 / 16d),
        VoxelShapes.box(4 / 16d, 7 / 16d, 13 / 16d, 6 / 16d, 10 / 16d, 14 / 16d),
        VoxelShapes.box(9 / 16d, 0, 13 / 16d, 13 / 16d, 7 / 16d, 15 / 16d),
        VoxelShapes.box(10 / 16d, 7 / 16d, 13 / 16d, 12 / 16d, 10 / 16d, 14 / 16d)
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
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for(int i = 0; i < times; i++){
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1], VoxelShapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final EnumProperty<Direction> FACING = HorizontalBlock.FACING;

    public CoalGeneratorBlock(){
        super("coal_generator", true, Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).harvestLevel(1).harvestTool(ToolType.PICKAXE).strength(1.5f, 6));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit){
        if(!worldIn.isClientSide)
            NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName(){
                    return null;
                }

                @Override
                public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player){
                    return new CoalGeneratorContainer(windowId, player, pos);
                }
            }, pos);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        int range = 2 * WormholeConfig.coalGeneratorRange.get() + 1;
        tooltip.add(new TranslationTextComponent("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower.get())).withStyle(TextFormatting.AQUA));

        CompoundNBT tag = stack.getOrCreateTag().contains("tileData") ? stack.getOrCreateTag().getCompound("tileData") : null;
        int energy = tag == null || tag.isEmpty() || !tag.contains("energy") ? 0 : tag.getInt("energy");
        tooltip.add(new StringTextComponent(EnergyFormat.formatCapacity(energy, WormholeConfig.coalGeneratorCapacity.get())).withStyle(TextFormatting.YELLOW));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos){
        return state.getValue(LIT) ? 8 : 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return new CoalGeneratorTile();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(LIT, FACING);
    }
}
