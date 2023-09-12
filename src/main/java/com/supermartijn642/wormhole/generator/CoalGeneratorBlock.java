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
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
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
        SHAPES[EnumFacing.NORTH.getHorizontalIndex()] = SHAPE;
        SHAPES[EnumFacing.EAST.getHorizontalIndex()] = SHAPE.rotate(EnumFacing.Axis.Y);
        SHAPES[EnumFacing.SOUTH.getHorizontalIndex()] = SHAPE.rotate(EnumFacing.Axis.Y).rotate(EnumFacing.Axis.Y);
        SHAPES[EnumFacing.WEST.getHorizontalIndex()] = SHAPE.rotate(EnumFacing.Axis.Y).rotate(EnumFacing.Axis.Y).rotate(EnumFacing.Axis.Y);
    }

    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;

    public CoalGeneratorBlock(){
        super(true, BlockProperties.create(Material.IRON, MapColor.GRAY).sound(SoundType.METAL).requiresCorrectTool().destroyTime(1.2f).explosionResistance(6));
        this.setDefaultState(this.getDefaultState().withProperty(LIT, false).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(!level.isRemote)
            CommonUtils.openContainer(new CoalGeneratorContainer(player, pos));
        return InteractionFeedback.CONSUME;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        int range = 2 * WormholeConfig.coalGeneratorRange.get() + 1;
        info.accept(TextComponents.translation("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower.get())).color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData", Constants.NBT.TAG_COMPOUND) ? stack.getTagCompound().getCompoundTag("tileData") : null;
        int energy = tag == null || tag.hasNoTags() || !tag.hasKey("energy", Constants.NBT.TAG_INT) ? 0 : tag.getInteger("energy");
        info.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, WormholeConfig.coalGeneratorCapacity.get())).color(TextFormatting.YELLOW).get());
    }

    @Override
    public IBlockState getStateForPlacement(World level, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState){
        SHAPES[state.getValue(FACING).getHorizontalIndex()].forEachBox(
            box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess level, BlockPos pos){
        return SHAPES[state.getValue(FACING).getHorizontalIndex()].simplify();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess level, BlockPos pos){
        return state.getValue(LIT) ? 8 : 0;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return Wormhole.coal_generator_tile.createBlockEntity();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, LIT, FACING);
    }

    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getHorizontalIndex() + (state.getValue(LIT) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(LIT, (meta & 4) == 4);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state){
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
        return BlockFaceShape.UNDEFINED;
    }
}
