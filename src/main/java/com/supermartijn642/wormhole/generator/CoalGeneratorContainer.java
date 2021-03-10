package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.gui.TileEntityBaseContainer;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorContainer extends TileEntityBaseContainer<CoalGeneratorTile> {

    public CoalGeneratorContainer(int id, PlayerEntity player, BlockPos pos){
        super(Wormhole.coal_generator_container, id, player, pos);
        this.addSlots();
    }

    @Override
    protected void addSlots(PlayerEntity player, CoalGeneratorTile tile){
        this.addSlot(new SlotItemHandler(tile, 0, 79, 52) {
            @Override
            public boolean canTakeStack(PlayerEntity playerIn){
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
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index){
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
