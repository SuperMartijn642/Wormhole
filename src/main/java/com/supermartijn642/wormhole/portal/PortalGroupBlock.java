package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

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
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return this.blockEntityType.get().create(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving){
        if(state.getBlock() != newState.getBlock()){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof IPortalGroupEntity)
                ((IPortalGroupEntity)entity).onBreak();
            level.removeBlockEntity(pos);
        }
    }
}
