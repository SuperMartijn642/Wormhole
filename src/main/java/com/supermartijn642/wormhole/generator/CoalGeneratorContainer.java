package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.gui.BlockEntityBaseContainer;
import com.supermartijn642.wormhole.Wormhole;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorContainer extends BlockEntityBaseContainer<CoalGeneratorBlockEntity> {

    public CoalGeneratorContainer(Player player, BlockPos pos){
        super(Wormhole.coal_generator_container, player, player.level, pos);
        this.addSlots();
    }

    @Override
    protected void addSlots(Player player, CoalGeneratorBlockEntity entity){
        this.addSlot(new DummySlot(0, 79, 52) {
            @Override
            public ItemStack getItem(){
                return object.getStack();
            }

            @Override
            public void set(ItemStack stack){
                object.setStack(stack);
            }

            @Override
            public boolean mayPlace(ItemStack stack){
                return object.isItemValid(stack.getItem());
            }
        });

        this.addPlayerSlots(7, 83);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index){
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
