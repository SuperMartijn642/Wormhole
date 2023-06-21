package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity implements IItemHandlerModifiable {

    private int burnTime = 0, totalBurnTime = 0;
    private ItemStack stack = ItemStack.EMPTY;

    public CoalGeneratorBlockEntity(){
        super(Wormhole.coal_generator_tile,
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
        int burnTime = this.stack.isEmpty() ? 0 : TileEntityFurnace.getItemBurnTime(this.stack);
        if(burnTime > 0){
            this.burnTime = this.totalBurnTime = burnTime;
            this.stack.shrink(1);
            this.dataChanged();
        }

        IBlockState state = this.world.getBlockState(this.pos);
        boolean lit = this.getBlockState().getValue(CoalGeneratorBlock.LIT);
        if(lit != this.burnTime > 0)
            this.world.setBlockState(this.pos, state.withProperty(CoalGeneratorBlock.LIT, !lit));
    }

    @Override
    public boolean hasCapability(Capability<?> cap, @Nullable EnumFacing side){
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing side){
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        return super.getCapability(cap, side);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound data = super.writeData();
        data.setInteger("burnTime", this.burnTime);
        data.setInteger("totalBurnTime", this.totalBurnTime);
        data.setTag("stack", this.stack.writeToNBT(new NBTTagCompound()));
        return data;
    }

    @Override
    protected void readData(NBTTagCompound tag){
        super.readData(tag);
        this.burnTime = tag.hasKey("burnTime") ? tag.getInteger("burnTime") : 0;
        this.totalBurnTime = tag.hasKey("totalBurnTime") ? tag.getInteger("totalBurnTime") : 0;
        this.stack = new ItemStack(tag.getCompoundTag("stack"));
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
        if(stack.isEmpty() || (!this.stack.isEmpty() && (!ItemStack.areItemsEqual(this.stack, stack) ||
            !ItemStack.areItemStackTagsEqual(this.stack, stack))))
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
        return Math.floor(TileEntityFurnace.getItemBurnTime(stack) / 2.5) > 0;
    }
}
