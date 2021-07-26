package com.supermartijn642.wormhole;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Created 11/4/2020 by SuperMartijn642
 */
public abstract class WormholeTile extends TileEntity {

    private boolean dataChanged = false;

    public WormholeTile(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    public void dataChanged(){
        this.dataChanged = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2 & 4);
    }

    protected abstract CompoundNBT writeData();

    protected CompoundNBT writeClientData(){
        return this.writeData();
    }

    protected abstract void readData(CompoundNBT tag);

    @Override
    public CompoundNBT save(CompoundNBT compound){
        super.save(compound);
        CompoundNBT data = this.writeData();
        if(data != null && !data.isEmpty())
            compound.put("data", this.writeData());
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt){
        super.load(state, nbt);
        this.readData(nbt.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.save(new CompoundNBT());
        tag.put("data", this.writeClientData());
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag){
        super.load(state, tag);
        this.readData(tag.getCompound("data"));
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new SUpdateTileEntityPacket(this.worldPosition, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.readData(pkt.getTag());
    }
}
