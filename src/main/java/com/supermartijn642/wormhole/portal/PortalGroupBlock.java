package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.WormholeBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalGroupBlock extends WormholeBlock {

    private final Supplier<? extends TileEntity> tileSupplier;

    public PortalGroupBlock(String registryName, Supplier<? extends TileEntity> tileSupplier, Material material, MapColor mapColor, SoundType soundType, float hardness, float resistance){
        super(registryName, true, material, mapColor, soundType, hardness, resistance);
        this.tileSupplier = tileSupplier;
    }

    public PortalGroupBlock(String registryName, Supplier<? extends TileEntity> tileSupplier){
        this(registryName, tileSupplier, Material.IRON, MapColor.GRAY, SoundType.METAL, 1.5f, 6);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.tileSupplier.get();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof IPortalGroupTile)
            ((IPortalGroupTile)tile).onBreak();

        super.breakBlock(worldIn, pos, state);
    }
}
