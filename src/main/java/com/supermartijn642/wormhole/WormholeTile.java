package com.supermartijn642.wormhole;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 11/4/2020 by SuperMartijn642
 */
public abstract class WormholeTile extends TileEntity {

    private boolean dataChanged = false;

    /**
     * used for dropping this item
     */
    public boolean destroyedByCreativePlayer = false;

    public WormholeTile(){
        super();
    }

    public void dataChanged(){
        this.dataChanged = true;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2 & 4);
    }

    protected abstract NBTTagCompound writeData();

    protected NBTTagCompound writeClientData(){
        return this.writeData();
    }

    protected abstract void readData(NBTTagCompound tag);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        NBTTagCompound data = this.writeData();
        if(data != null && !data.hasNoTags())
            compound.setTag("data", this.writeData());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        this.readData(nbt.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
        tag.setTag("data", this.writeClientData());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.readFromNBT(tag);
        this.readData(tag.getCompoundTag("data"));
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new SPacketUpdateTileEntity(this.pos, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.readData(pkt.getNbtCompound());
    }

    public IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate){
        return oldState.getBlock() != newSate.getBlock();
    }
}
