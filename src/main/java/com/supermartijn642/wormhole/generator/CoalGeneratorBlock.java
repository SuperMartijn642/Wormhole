package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeBlock;
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
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlock extends WormholeBlock {

    private static final AxisAlignedBB[] SHAPE = new AxisAlignedBB[]{
        new AxisAlignedBB(2 / 16d, 0, 1 / 16d, 14 / 16d, 12 / 16d, 13 / 16d),
        new AxisAlignedBB(3 / 16d, 0, 13 / 16d, 7 / 16d, 7 / 16d, 15 / 16d),
        new AxisAlignedBB(4 / 16d, 7 / 16d, 13 / 16d, 6 / 16d, 10 / 16d, 14 / 16d),
        new AxisAlignedBB(9 / 16d, 0, 13 / 16d, 13 / 16d, 7 / 16d, 15 / 16d),
        new AxisAlignedBB(10 / 16d, 7 / 16d, 13 / 16d, 12 / 16d, 10 / 16d, 14 / 16d)
    };
    private static final AxisAlignedBB[][] SHAPES = new AxisAlignedBB[4][];

    static{
        SHAPES[EnumFacing.NORTH.getHorizontalIndex()] = SHAPE;
        SHAPES[EnumFacing.EAST.getHorizontalIndex()] = rotateShape(EnumFacing.NORTH, EnumFacing.EAST, SHAPE);
        SHAPES[EnumFacing.SOUTH.getHorizontalIndex()] = rotateShape(EnumFacing.NORTH, EnumFacing.SOUTH, SHAPE);
        SHAPES[EnumFacing.WEST.getHorizontalIndex()] = rotateShape(EnumFacing.NORTH, EnumFacing.WEST, SHAPE);
    }

    public static AxisAlignedBB[] rotateShape(EnumFacing from, EnumFacing to, AxisAlignedBB[] shape){
        AxisAlignedBB[] result = new AxisAlignedBB[shape.length];
        System.arraycopy(shape, 0, result, 0, shape.length);

        int times = (to.getHorizontalIndex() - from.getHorizontalIndex() + 4) % 4;
        for(int i = 0; i < times; i++){
            for(int j = 0; j < result.length; j++){
                AxisAlignedBB box = result[j];
                result[j] = new AxisAlignedBB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
            }
        }

        return result;
    }

    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;

    public CoalGeneratorBlock(){
        super("coal_generator", true, Material.IRON, MapColor.GRAY, SoundType.METAL, 1.5f, 6);
        this.setHarvestLevel("pickaxe", 1);
        this.setDefaultState(this.getDefaultState().withProperty(LIT, false).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        playerIn.openGui(Wormhole.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        int range = 2 * WormholeConfig.coalGeneratorRange + 1;
        tooltip.add(new TextComponentTranslation("wormhole.coal_generator.info", range, EnergyFormat.formatEnergyPerTick(WormholeConfig.coalGeneratorPower)).setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());

        NBTTagCompound tag = stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData") ? stack.getTagCompound().getCompoundTag("tileData") : null;
        int energy = tag == null || tag.hasNoTags() || !tag.hasKey("energy") ? 0 : tag.getInteger("energy");
        tooltip.add(new TextComponentString(EnergyFormat.formatCapacity(energy, WormholeConfig.coalGeneratorCapacity)).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState){
        Arrays.stream(SHAPES[state.getValue(FACING).getHorizontalIndex()]).forEach(
            box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        AxisAlignedBB result = null;
        for(AxisAlignedBB box : SHAPES[state.getValue(FACING).getHorizontalIndex()]){
            if(result == null)
                result = box;
            else
                result = new AxisAlignedBB(
                    Math.min(result.minX, box.minX),
                    Math.min(result.minY, box.minY),
                    Math.min(result.minZ, box.minZ),
                    Math.max(result.maxX, box.maxX),
                    Math.max(result.maxY, box.maxY),
                    Math.max(result.maxZ, box.maxZ)
                );
        }
        return result;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos){
        return state.getValue(LIT) ? 8 : 0;
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return new CoalGeneratorTile();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, LIT, FACING);
    }

    @Override
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
