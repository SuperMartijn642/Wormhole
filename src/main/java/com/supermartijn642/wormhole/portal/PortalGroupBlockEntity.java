package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupBlockEntity extends BaseBlockEntity implements TickableBlockEntity, IPortalGroupEntity {

    public PortalGroupBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
        super(blockEntityType, pos, state);
    }

    @Override
    public boolean hasGroup(){
        PortalGroupCapability groups = this.level.getCapability(PortalGroupCapability.CAPABILITY).orElse(null);
        return groups != null && groups.getGroup(this) != null;
    }

    @Override
    public PortalGroup getGroup(){
        return this.level.getCapability(PortalGroupCapability.CAPABILITY).lazyMap(groups -> groups.getGroup(this)).orElse(null);
    }

    @Override
    public void update(){
        if(this.hasGroup())
            this.getGroup().tick();
    }

    @Override
    protected void writeData(ValueOutput output){
    }

    @Override
    protected void readData(ValueInput input){
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state){
        if(this.hasGroup())
            this.getGroup().destroy();
    }
}
