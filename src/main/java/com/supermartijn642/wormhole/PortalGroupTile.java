package com.supermartijn642.wormhole;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupTile extends TileEntity implements IPortalGroupTile, ITickable {
    protected PortalGroup group;

    public PortalGroupTile(){
        super();
    }

    @Override
    public void setGroup(PortalGroup group){
        this.group = group;
    }

    @Override
    public boolean hasGroup(){
        return this.group != null;
    }

    @Override
    public void update(){
        if(this.group != null && this.group.isController(this))
            this.group.tick(this.world);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        if(this.group != null && this.group.isController(this))
            compound.setTag("group", this.group.write());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        if(compound.hasKey("group"))
            this.group = new PortalGroup(compound.getCompoundTag("group"));
    }

    @Override
    public void onBreak(){
        if(this.group != null)
            this.group.invalidate();
    }
}
