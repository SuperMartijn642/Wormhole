package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.gui.BlockEntityBaseContainer;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorContainer extends BlockEntityBaseContainer<CoalGeneratorBlockEntity> {

    public CoalGeneratorContainer(EntityPlayer player, BlockPos pos){
        super(Wormhole.coal_generator_container, player, player.world, pos);
        this.addSlots();
    }

    @Override
    protected void addSlots(EntityPlayer player, CoalGeneratorBlockEntity entity){
        this.addSlot(new SlotItemHandler(entity, 0, 79, 52) {
            @Override
            public boolean canTakeStack(EntityPlayer player){
                return true;
            }

            @Override
            public ItemStack decrStackSize(int amount){
                ItemStack stack = entity.getStackInSlot(0);
                if(amount >= stack.getCount()){
                    entity.setStackInSlot(0, ItemStack.EMPTY);
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index){
        ItemStack returnStack = ItemStack.EMPTY;

        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack()){
            ItemStack slotStack = slot.getStack();
            returnStack = slotStack.copy();
            if(index == 0){
                if(!this.mergeItemStack(slotStack, 1, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            }else if(!this.mergeItemStack(slotStack, 0, 1, false))
                return ItemStack.EMPTY;

            if(slotStack.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }

        return returnStack;
    }

    public CoalGeneratorBlockEntity getBlockEntity(){
        return this.object;
    }

    public BlockPos getBlockEntityPos(){
        return this.blockEntityPos;
    }
}
