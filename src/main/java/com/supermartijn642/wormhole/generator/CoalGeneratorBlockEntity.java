package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity implements IItemHandlerModifiable {

    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> this);
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
                ItemStackTemplate remainder = this.stack.getCraftingRemainder();
                this.stack = remainder == null ? ItemStack.EMPTY : remainder.create();
            }else
                this.stack.shrink(1);
            this.dataChanged();
        }

        BlockState state = this.level.getBlockState(this.worldPosition);
        boolean lit = this.getBlockState().getValue(CoalGeneratorBlock.LIT);
        if(lit != this.burnTime > 0)
            this.level.setBlockAndUpdate(this.worldPosition, state.setValue(CoalGeneratorBlock.LIT, !lit));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
        if(cap == ForgeCapabilities.ITEM_HANDLER)
            return this.itemCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void writeData(ValueOutput output){
        super.writeData(output);
        output.putInt("burnTime", this.burnTime);
        output.putInt("totalBurnTime", this.totalBurnTime);
        if(!this.stack.isEmpty())
            output.store("stack", ItemStack.CODEC, this.stack);
    }

    @Override
    protected void readData(ValueInput input){
        super.readData(input);
        this.burnTime = input.getIntOr("burnTime", 0);
        this.totalBurnTime = input.getIntOr("totalBurnTime", 0);
        this.stack = input.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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
        if(stack.isEmpty() || (!this.stack.isEmpty() && !ItemStack.isSameItemSameComponents(this.stack, stack)))
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
        return this.getBurnTime(stack) > 0;
    }

    private int getBurnTime(ItemStack stack){
        return (int)Math.floor(this.level.fuelValues().burnDuration(stack, RecipeType.SMELTING) / 2.5);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.itemCapability.invalidate();
    }
}
