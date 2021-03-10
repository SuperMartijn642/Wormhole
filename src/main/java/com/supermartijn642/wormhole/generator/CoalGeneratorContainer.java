package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.gui.TileEntityBaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorContainer extends TileEntityBaseContainer<CoalGeneratorTile> {

    public CoalGeneratorContainer(EntityPlayer player, BlockPos pos){
        super(player, pos);
        this.addSlots();
    }

    @Override
    protected void addSlots(EntityPlayer player, CoalGeneratorTile tile){
        this.addSlot(new SlotItemHandler(tile, 0, 79, 52) {
            @Override
            public boolean canTakeStack(EntityPlayer playerIn){
                return true;
            }

            @Override
            public ItemStack decrStackSize(int amount){
                ItemStack stack = tile.getStackInSlot(0);
                if(amount >= stack.getCount()){
                    tile.setStackInSlot(0, ItemStack.EMPTY);
                    return stack;
                }
                ItemStack result = stack.copy();
                result.setCount(Math.min(amount, stack.getCount()));
                stack.shrink(amount);
                return result;
            }
        });

        this.addPlayerSlots(7, 83);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index){
        ItemStack itemstack = ItemStack.EMPTY;

        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack()){
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if(index == 0){
                if(!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            }else if(!this.mergeItemStack(itemstack1, 0, 1, false))
                return ItemStack.EMPTY;

            if(itemstack1.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }

        return itemstack;
    }
}
