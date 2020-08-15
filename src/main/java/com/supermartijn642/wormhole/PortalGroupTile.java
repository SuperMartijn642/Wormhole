package com.supermartijn642.wormhole;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroupTile extends TileEntity implements IPortalGroupTile, ITickableTileEntity {
    protected PortalGroup group;

    public PortalGroupTile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
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
    public void tick(){
        if(this.group != null && this.group.isController(this))
            this.group.tick(this.world);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        if(this.group != null && this.group.isController(this))
            compound.put("group", this.group.write());
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound){
        super.read(state, compound);
        if(compound.contains("group"))
            this.group = new PortalGroup(compound.getCompound("group"));
    }

    @Override
    public void onBreak(){
        if(this.group != null)
            this.group.invalidate();
    }
}
