package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.gui.BlockEntityBaseContainer;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorContainer extends BlockEntityBaseContainer<CoalGeneratorBlockEntity> {

    public CoalGeneratorContainer(PlayerEntity player, BlockPos pos){
        super(Wormhole.coal_generator_container, player, player.level, pos);
        this.addSlots();
    }

    @Override
    protected void addSlots(PlayerEntity player, CoalGeneratorBlockEntity entity){
        this.addSlot(new SlotItemHandler(entity, 0, 79, 52) {
            @Override
            public boolean mayPickup(PlayerEntity player){
                return true;
            }

            @Override
            public ItemStack remove(int amount){
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
    public ItemStack quickMoveStack(PlayerEntity player, int index){
        ItemStack returnStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if(slot != null && slot.hasItem()){
            ItemStack slotStack = slot.getItem();
            returnStack = slotStack.copy();
            if(index == 0){
                if(!this.moveItemStackTo(slotStack, 1, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }else if(!this.moveItemStackTo(slotStack, 0, 1, false))
                return ItemStack.EMPTY;

            if(slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
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
