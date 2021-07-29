package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.WormholeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupTile extends WormholeTile implements IPortalGroupTile {

    public PortalGroupTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state){
        super(tileEntityTypeIn, pos, state);
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

    public void tick(){
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
