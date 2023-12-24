package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupBlockEntity extends BaseBlockEntity implements TickableBlockEntity, IPortalGroupEntity {

    public PortalGroupBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
        super(blockEntityType, pos, state);
    }

    @Override
    public boolean hasGroup(){
        return PortalGroupCapability.get(this.level).getGroup(this) != null;
    }

    @Override
    public PortalGroup getGroup(){
        return PortalGroupCapability.get(this.level).getGroup(this);
    }

    @Override
    public void update(){
        if(this.hasGroup())
            this.getGroup().tick();
    }

    @Override
    protected CompoundTag writeData(){
        return new CompoundTag();
    }

    @Override
    protected void readData(CompoundTag tag){
    }

    @Override
    public void onBreak(){
        if(this.hasGroup())
            this.getGroup().destroy();
    }
}
