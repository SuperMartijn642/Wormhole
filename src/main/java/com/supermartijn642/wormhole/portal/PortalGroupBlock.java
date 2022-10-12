package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class PortalGroupBlock extends BaseBlock implements EntityHoldingBlock {

    private final Supplier<BaseBlockEntityType<?>> blockEntityType;

    public PortalGroupBlock(BlockProperties properties, Supplier<BaseBlockEntityType<?>> blockEntityType){
        super(true, properties);
        this.blockEntityType = blockEntityType;
    }

    public PortalGroupBlock(Supplier<BaseBlockEntityType<?>> blockEntityType){
        this(BlockProperties.create(Material.METAL, MaterialColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectTool().destroyTime(1.5f).explosionResistance(6).noOcclusion(), blockEntityType);
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return this.blockEntityType.get().create();
    }

    @Override
    public void onRemove(BlockState state, World level, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            TileEntity entity = level.getBlockEntity(pos);
            if(entity instanceof IPortalGroupEntity)
                ((IPortalGroupEntity)entity).onBreak();
            level.removeBlockEntity(pos);
        }
    }
}
