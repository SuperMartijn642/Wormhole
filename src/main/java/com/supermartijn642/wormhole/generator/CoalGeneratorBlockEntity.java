package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity {

    private final Storage<ItemVariant> itemCapability = new SingleStackStorage() {
        @Override
        protected boolean canInsert(ItemVariant itemVariant){
            return CoalGeneratorBlockEntity.this.isItemValid(itemVariant.getItem());
        }

        @Override
        protected boolean canExtract(ItemVariant itemVariant){
            return false;
        }

        @Override
        protected ItemStack getStack(){
            return CoalGeneratorBlockEntity.this.getStack();
        }

        @Override
        protected void setStack(ItemStack stack){
            CoalGeneratorBlockEntity.this.setStack(stack);
        }
    };
    private Map<Item,Integer> burnTimes;
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
        int burnTime = this.stack.isEmpty() ? 0 : this.getBurnTime(this.stack.getItem());
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

    public Storage<ItemVariant> getItemCapability(){
        return this.itemCapability;
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

    public ItemStack getStack(){
        return stack;
    }

    public void setStack(ItemStack stack){
        this.stack = stack;
        this.dataChanged();
    }

    public boolean isItemValid(Item item){
        return this.getBurnTime(item) > 0;
    }

    private int getBurnTime(Item item){
        if(this.burnTimes == null)
            this.burnTimes = AbstractFurnaceBlockEntity.getFuel();
        return (int)Math.floor(this.burnTimes.getOrDefault(item, 0) / 2.5);
    }
}
