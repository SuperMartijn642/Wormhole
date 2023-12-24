package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity implements IItemHandlerModifiable {

    private int burnTime = 0, totalBurnTime = 0;
    private ItemStack stack = ItemStack.EMPTY;

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state){
        super(Wormhole.coal_generator_tile,
            pos,
            state,
            WormholeConfig.coalGeneratorCapacity.get(),
            WormholeConfig.coalGeneratorRange.get(),
            WormholeConfig.coalGeneratorPower.get() * 2);
    }

    @Override
    public void update(){
        super.update();

        if(this.energy < this.energyCapacity){
            if(this.burnTime > 0){
                this.burnTime--;
                this.energy += WormholeConfig.coalGeneratorPower.get();
                if(this.energy > this.energyCapacity)
                    this.energy = this.energyCapacity;
                if(this.burnTime == 0){
                    this.totalBurnTime = 0;
                    this.burnItem();
                }
                this.dataChanged();
            }else
                this.burnItem();
        }
    }

    private void burnItem(){
        int burnTime = this.stack.isEmpty() ? 0 : CommonHooks.getBurnTime(this.stack, RecipeType.SMELTING);
        if(burnTime > 0){
            this.burnTime = this.totalBurnTime = burnTime;
            this.stack.shrink(1);
            this.dataChanged();
        }

        BlockState state = this.level.getBlockState(this.worldPosition);
        boolean lit = this.getBlockState().getValue(CoalGeneratorBlock.LIT);
        if(lit != this.burnTime > 0)
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(CoalGeneratorBlock.LIT, !lit));
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag data = super.writeData();
        data.putInt("burnTime", this.burnTime);
        data.putInt("totalBurnTime", this.totalBurnTime);
        data.put("stack", this.stack.save(new CompoundTag()));
        return data;
    }

    @Override
    protected void readData(CompoundTag tag){
        super.readData(tag);
        this.burnTime = tag.contains("burnTime") ? tag.getInt("burnTime") : 0;
        this.totalBurnTime = tag.contains("totalBurnTime") ? tag.getInt("totalBurnTime") : 0;
        this.stack = ItemStack.of(tag.getCompound("stack"));
    }

    public float getProgress(){
        return this.totalBurnTime == 0 ? 0 : (float)this.burnTime / this.totalBurnTime;
    }

    @Override
    public int getSlots(){
        return 1;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack){
        if(slot == 0)
            this.stack = stack.copy();
    }

    @Override
    public ItemStack getStackInSlot(int slot){
        return this.stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
        if(stack.isEmpty() || (!this.stack.isEmpty() && !ItemStack.isSameItemSameTags(this.stack, stack)))
            return stack;

        int count = Math.min(stack.getMaxStackSize() - this.stack.getCount(), stack.getCount());
        if(!simulate){
            ItemStack newStack = stack.copy();
            newStack.setCount(this.stack.getCount() + count);
            this.stack = newStack;
        }

        ItemStack result = stack.copy();
        result.shrink(count);
        return result;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate){
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot){
        return this.stack.isEmpty() ? 64 : this.stack.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack){
        return Math.floor(CommonHooks.getBurnTime(stack, RecipeType.SMELTING) / 2.5) > 0;
    }
}
