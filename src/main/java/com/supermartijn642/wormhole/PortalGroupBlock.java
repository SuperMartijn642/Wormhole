package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalGroupBlock extends Block {

    private final Supplier<? extends TileEntity> tileSupplier;

    public PortalGroupBlock(Properties properties, String registryName, Supplier<? extends TileEntity> tileSupplier){
        super(properties);
        this.setRegistryName(registryName);
        this.tileSupplier = tileSupplier;
    }

    public PortalGroupBlock(String registryName, Supplier<? extends TileEntity> tileSupplier){
        this(Properties.create(Material.IRON, MaterialColor.GRAY).sound(SoundType.METAL).harvestLevel(1).harvestTool(ToolType.PICKAXE), registryName, tileSupplier);
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return this.tileSupplier.get();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof IPortalGroupTile)
                ((IPortalGroupTile)tile).onBreak();
            worldIn.removeTileEntity(pos);
        }
    }
}
