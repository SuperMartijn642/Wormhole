package com.supermartijn642.wormhole;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroup {

    public final PortalShape shape;
    private PortalTarget target;
    public final BlockPos controller;
    private World world;
    private boolean initialized = false;

    public PortalGroup(PortalShape shape){
        this.shape = shape;
        this.controller = shape.stabilizers.get(0);
    }

    public PortalGroup(NBTTagCompound tag){
        this.shape = PortalShape.read(tag.getCompoundTag("shape"));
        this.target = tag.hasKey("target") ? PortalTarget.read(tag.getCompoundTag("target")) : null;
        this.controller = new BlockPos(tag.getInteger("controllerX"), tag.getInteger("controllerY"), tag.getInteger("controllerZ"));
    }

    public void tick(World world){
        if(!this.initialized){
            this.world = world;
            for(BlockPos pos : this.shape.frame){
                TileEntity tile = this.world.getTileEntity(pos);
                if(tile instanceof IPortalGroupTile)
                    ((IPortalGroupTile)tile).setGroup(this);
            }
            if(this.target != null){
                for(BlockPos pos : this.shape.area){
                    TileEntity tile = this.world.getTileEntity(pos);
                    if(tile instanceof IPortalGroupTile)
                        ((IPortalGroupTile)tile).setGroup(this);
                }
            }
            this.initialized = true;
        }
    }

    public void setTarget(PortalTarget target){
        if(target == null){
            this.removeTarget();
            return;
        }
        this.target = target;
        this.shape.createPortals(this.world, this);
        this.updateStabilizers(true);
    }

    public void removeTarget(){
        if(this.target != null){
            this.target = null;
            this.shape.destroyPortals(this.world);
        }
        this.updateStabilizers(false);
    }

    public PortalTarget getTarget(){
        return this.target;
    }

    private void updateStabilizers(boolean on){
        for(BlockPos pos : this.shape.frame){
            IBlockState state = this.world.getBlockState(pos);
            if(state.getBlock() instanceof StabilizerBlock && state.getValue(StabilizerBlock.ON_PROPERTY) != on){
                this.world.setBlockState(pos, state.withProperty(StabilizerBlock.ON_PROPERTY, on));
                TileEntity tile = this.world.getTileEntity(pos);
                if(tile instanceof StabilizerTile)
                    ((StabilizerTile)tile).setGroup(this);
            }
        }
    }

    public boolean isController(TileEntity tile){
        return this.controller.equals(tile.getPos());
    }

    public void invalidate(){
        this.shape.destroyPortals(this.world);
        for(BlockPos pos : this.shape.frame){
            TileEntity tile = this.world.getTileEntity(pos);
            if(tile instanceof PortalGroupTile)
                ((PortalGroupTile)tile).setGroup(null);
        }
    }

    public NBTTagCompound write(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("shape", this.shape.write());
        if(this.target != null)
            tag.setTag("target", this.target.write());
        tag.setInteger("controllerX", this.controller.getX());
        tag.setInteger("controllerY", this.controller.getY());
        tag.setInteger("controllerZ", this.controller.getZ());
        return tag;
    }

}
