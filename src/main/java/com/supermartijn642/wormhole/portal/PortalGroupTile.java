package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.WormholeTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupTile extends WormholeTile implements IPortalGroupTile, ITickable {

    @Override
    public boolean hasGroup(){
        PortalGroupCapability groups = this.world.getCapability(PortalGroupCapability.CAPABILITY, null);
        return groups != null && groups.getGroup(this) != null;
    }

    @Override
    public PortalGroup getGroup(){
        PortalGroupCapability groups = this.world.getCapability(PortalGroupCapability.CAPABILITY, null);
        return groups == null ? null : groups.getGroup(this);
    }

    @Override
    public void update(){
        if(this.hasGroup())
            this.getGroup().tick();
    }

    @Override
    protected NBTTagCompound writeData(){
        return new NBTTagCompound();
    }

    @Override
    protected void readData(NBTTagCompound tag){
    }

    @Override
    public void onBreak(){
        if(this.hasGroup())
            this.getGroup().destroy();
    }
}
