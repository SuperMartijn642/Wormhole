package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class WormholeBlock extends Block {

    private final boolean saveTileData;

    public WormholeBlock(String registryName, boolean saveTileData, Material material, MapColor mapColor, SoundType soundType, float hardness, float resistance){
        super(material, mapColor);
        this.setRegistryName(registryName);
        this.setUnlocalizedName("wormhole." + registryName);
        this.saveTileData = saveTileData;

        this.setSoundType(soundType);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        NBTTagCompound tag = stack.getTagCompound();
        tag = tag != null && tag.hasKey("tileData") ? tag.getCompoundTag("tileData") : null;
        if(tag == null || tag.hasNoTags())
            return;

        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof WormholeTile)
            ((WormholeTile)tile).readData(tag);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof WormholeTile)
            ((WormholeTile)tile).destroyedByCreativePlayer = player.capabilities.isCreativeMode;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        for(ItemStack drop : this.getActualDrops(worldIn, pos, state, 0))
            spawnAsEntity(worldIn, pos, drop);

//        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
    }

    public List<ItemStack> getActualDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        NonNullList<ItemStack> drops = NonNullList.create();

        super.getDrops(drops, world, pos, state, fortune);

        if(!this.saveTileData)
            return drops;

        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof WormholeTile))
            return drops;

        if(((WormholeTile)tile).destroyedByCreativePlayer)
            return NonNullList.create();

        NBTTagCompound tileTag = ((WormholeTile)tile).writeData();
        if(tileTag == null || tileTag.hasNoTags())
            return drops;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", tileTag);

        for(ItemStack stack : drops){
            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this){
                stack.setTagCompound(tag);
            }
        }

        return drops;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof WormholeTile))
            return stack;

        NBTTagCompound tileTag = ((WormholeTile)tile).writeData();
        if(tileTag == null || tileTag.hasNoTags())
            return stack;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", tileTag);

        if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this)
            stack.setTagCompound(tag);

        return stack;
    }
}
