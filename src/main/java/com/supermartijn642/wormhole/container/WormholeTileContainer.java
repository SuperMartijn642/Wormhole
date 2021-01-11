package com.supermartijn642.wormhole.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 12/20/2020 by SuperMartijn642
 */
public abstract class WormholeTileContainer<T extends TileEntity> extends Container {

    public final PlayerEntity player;
    public final World world;
    public final BlockPos pos;

    public WormholeTileContainer(ContainerType<?> type, int id, PlayerEntity player, BlockPos pos){
        super(type, id);
        this.player = player;
        this.world = player.world;
        this.pos = pos;

        T tile = this.getTileOrClose();
        if(tile != null)
            this.addSlots(tile, player);
    }

    protected abstract void addSlots(T tile, PlayerEntity player);

    protected void addPlayerSlots(int x, int y){
        // player
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                this.addSlot(new Slot(this.player.inventory, row * 9 + column + 9, x + 18 * column, y + 18 * row));
            }
        }

        // hot bar
        for(int column = 0; column < 9; column++)
            this.addSlot(new Slot(this.player.inventory, column, x + 18 * column, y + 58));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn){
        return this.pos.distanceSq(playerIn.getPosition()) < 64 * 64;
    }

    public T getTileOrClose(){
        if(this.world != null && this.pos != null){
            TileEntity tile = this.world.getTileEntity(this.pos);
            try{
                return (T)tile;
            }catch(ClassCastException ignore){}
        }
        this.player.closeScreen();
        return null;
    }
}
