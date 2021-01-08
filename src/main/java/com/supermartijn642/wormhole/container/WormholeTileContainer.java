package com.supermartijn642.wormhole.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 12/20/2020 by SuperMartijn642
 */
public abstract class WormholeTileContainer<T extends TileEntity> extends Container {

    public final EntityPlayer player;
    public final World world;
    public final BlockPos pos;

    public WormholeTileContainer(EntityPlayer player, BlockPos pos){
        this.player = player;
        this.world = player.world;
        this.pos = pos;

        T tile = this.getTileOrClose();
        if(tile != null)
            this.addSlots(tile, player);
    }

    protected abstract void addSlots(T tile, EntityPlayer player);

    protected void addPlayerSlots(int x, int y){
        // player
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                this.addSlotToContainer(new Slot(this.player.inventory, row * 9 + column + 9, x + 18 * column, y + 18 * row));
            }
        }

        // hot bar
        for(int column = 0; column < 9; column++)
            this.addSlotToContainer(new Slot(this.player.inventory, column, x + 18 * column, y + 58));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn){
        return true;
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
