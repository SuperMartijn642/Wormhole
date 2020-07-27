package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalGroupBlock extends Block {

    private final Supplier<? extends TileEntity> tileSupplier;

    public PortalGroupBlock(Material material, MapColor color, String registryName, Supplier<? extends TileEntity> tileSupplier){
        super(material, color);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(Wormhole.MODID + "." + registryName);
        this.tileSupplier = tileSupplier;
    }

    public PortalGroupBlock(String registryName, Supplier<? extends TileEntity> tileSupplier){
        this(Material.IRON, MapColor.GRAY, registryName, tileSupplier);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.tileSupplier.get();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof IPortalGroupTile)
            ((IPortalGroupTile)tile).onBreak();
        worldIn.removeTileEntity(pos);
    }
}
