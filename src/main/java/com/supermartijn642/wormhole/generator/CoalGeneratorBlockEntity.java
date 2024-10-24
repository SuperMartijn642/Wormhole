package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity {

    private final IItemHandler itemCapability = new IItemHandler() {
        @Override
        public int getSlots(){
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot){
            return CoalGeneratorBlockEntity.this.getStack();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
            ItemStack current = CoalGeneratorBlockEntity.this.getStack();
            if(stack.isEmpty() || (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)))
                return stack;

            int count = Math.min(stack.getMaxStackSize() - current.getCount(), stack.getCount());
            if(!simulate){
                ItemStack newStack = stack.copy();
                newStack.setCount(current.getCount() + count);
                CoalGeneratorBlockEntity.this.setStack(newStack);
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
            ItemStack current = CoalGeneratorBlockEntity.this.getStack();
            return current.isEmpty() ? 64 : current.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack){
            return CoalGeneratorBlockEntity.this.isItemValid(stack);
        }
    };
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
        int burnTime = this.stack.isEmpty() ? 0 : this.getBurnTime(this.stack);
        if(burnTime > 0){
            this.burnTime = this.totalBurnTime = burnTime;
            if(this.stack.getCount() == 1){
                ItemStack remainder = this.stack.getCraftingRemainder();
                this.stack = remainder == null ? ItemStack.EMPTY : remainder;
            }else
                this.stack.shrink(1);
            this.dataChanged();
        }

        BlockState state = this.level.getBlockState(this.worldPosition);
        boolean lit = this.getBlockState().getValue(CoalGeneratorBlock.LIT);
        if(lit != this.burnTime > 0)
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(CoalGeneratorBlock.LIT, !lit));
    }

    public IItemHandler getItemCapability(){
        return this.itemCapability;
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag data = super.writeData();
        data.putInt("burnTime", this.burnTime);
        data.putInt("totalBurnTime", this.totalBurnTime);
        data.put("stack", this.stack.saveOptional(this.level.registryAccess()));
        return data;
    }

    @Override
    protected void readData(CompoundTag tag){
        super.readData(tag);
        this.burnTime = tag.contains("burnTime") ? tag.getInt("burnTime") : 0;
        this.totalBurnTime = tag.contains("totalBurnTime") ? tag.getInt("totalBurnTime") : 0;
        this.stack = ItemStack.parseOptional(CommonUtils.getRegistryAccess(), tag.getCompound("stack"));
    }

    public float getProgress(){
        return this.totalBurnTime == 0 ? 0 : (float)this.burnTime / this.totalBurnTime;
    }

    public ItemStack getStack(){
        return stack;
    }

    public void setStack(ItemStack stack){
        this.stack = stack;
        this.dataChanged();
    }

    public boolean isItemValid(ItemStack stack){
        return this.getBurnTime(stack) > 0;
    }

    private int getBurnTime(ItemStack stack){
        return (int)Math.floor(stack.getBurnTime(RecipeType.SMELTING, this.level.fuelValues()) / 2.5);
    }
}
