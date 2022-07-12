package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.WormholeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalGroupBlock extends WormholeBlock implements EntityBlock {

    private final BiFunction<BlockPos,BlockState,? extends BlockEntity> tileSupplier;

    public PortalGroupBlock(Properties properties, String registryName, BiFunction<BlockPos,BlockState,? extends BlockEntity> tileSupplier){
        super(registryName, true, properties);
        this.tileSupplier = tileSupplier;
    }

    public PortalGroupBlock(String registryName, BiFunction<BlockPos,BlockState,? extends BlockEntity> tileSupplier){
        this(Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectToolForDrops().strength(1.5f, 6).noOcclusion(), registryName, tileSupplier);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return this.tileSupplier.apply(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if(tile instanceof IPortalGroupTile)
                ((IPortalGroupTile)tile).onBreak();
            worldIn.removeBlockEntity(pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> blockEntityType){
        return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntityType).getNamespace().equals("wormhole") ?
            (world2, pos, state2, entity) -> {
                if(entity instanceof PortalGroupTile)
                    ((PortalGroupTile)entity).tick();
            } : null;
    }
}
