package com.supermartijn642.wormhole;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 11/4/2020 by SuperMartijn642
 */
public abstract class WormholeTile extends BlockEntity {

    private boolean dataChanged = false;

    public WormholeTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state){
        super(tileEntityTypeIn, pos, state);
    }

    public void dataChanged(){
        this.dataChanged = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2 & 4);
    }

    protected abstract CompoundTag writeData();

    protected CompoundTag writeClientData(){
        return this.writeData();
    }

    protected abstract void readData(CompoundTag tag);

    @Override
    public CompoundTag save(CompoundTag compound){
        super.save(compound);
        CompoundTag data = this.writeData();
        if(data != null && !data.isEmpty())
            compound.put("data", this.writeData());
        return compound;
    }

    @Override
    public void load(CompoundTag nbt){
        super.load(nbt);
        this.readData(nbt.getCompound("data"));
    }

    @Override
    public CompoundTag getUpdateTag(){
        CompoundTag tag = super.save(new CompoundTag());
        tag.put("data", this.writeClientData());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag){
        super.load(tag);
        this.readData(tag.getCompound("data"));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
        this.readData(pkt.getTag());
    }
}
