package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.WormholeTile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupTile extends WormholeTile implements IPortalGroupTile, ITickableTileEntity {

    public PortalGroupTile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    @Override
    public boolean hasGroup(){
        PortalGroupCapability groups = this.world.getCapability(PortalGroupCapability.CAPABILITY).orElse(null);
        return groups != null && groups.getGroup(this) != null;
    }

    @Override
    public PortalGroup getGroup(){
        PortalGroupCapability groups = this.world.getCapability(PortalGroupCapability.CAPABILITY).orElse(null);
        return groups == null ? null : groups.getGroup(this);
    }

    @Override
    public void tick(){
        if(this.hasGroup())
            this.getGroup().tick();
    }

    @Override
    protected CompoundNBT writeData(){
        return new CompoundNBT();
    }

    @Override
    protected void readData(CompoundNBT tag){
    }

    @Override
    public void onBreak(){
        if(this.hasGroup())
            this.getGroup().destroy();
    }
}
