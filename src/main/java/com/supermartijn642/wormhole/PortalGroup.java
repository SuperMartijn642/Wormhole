package com.supermartijn642.wormhole;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
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

    public PortalGroup(CompoundNBT tag){
        this.shape = PortalShape.read(tag.getCompound("shape"));
        this.target = tag.contains("target") ? PortalTarget.read(tag.getCompound("target")) : null;
        this.controller = new BlockPos(tag.getInt("controllerX"), tag.getInt("controllerY"), tag.getInt("controllerZ"));
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
            BlockState state = this.world.getBlockState(pos);
            if(state.getBlock() instanceof StabilizerBlock && state.get(StabilizerBlock.ON_PROPERTY) != on)
                this.world.setBlockState(pos, state.with(StabilizerBlock.ON_PROPERTY, on));
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

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.put("shape", this.shape.write());
        if(this.target != null)
            tag.put("target", this.target.write());
        tag.putInt("controllerX", this.controller.getX());
        tag.putInt("controllerY", this.controller.getY());
        tag.putInt("controllerZ", this.controller.getZ());
        return tag;
    }

}
