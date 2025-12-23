package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Created 12/18/2020 by SuperMartijn642
 */
public class CoalGeneratorBlockEntity extends GeneratorBlockEntity {

    private final ResourceHandler<ItemResource> itemCapability = new ResourceHandler<>() {
        private final SnapshotJournal<ItemStack> snapshotJournal = new SnapshotJournal<>() {
            @Override
            protected ItemStack createSnapshot(){
                return CoalGeneratorBlockEntity.this.getStack();
            }

            @Override
            protected void revertToSnapshot(ItemStack snapshot){
                CoalGeneratorBlockEntity.this.setStack(snapshot);
            }
        };

        @Override
        public int size(){
            return 1;
        }

        @Override
        public ItemResource getResource(int index){
            return ItemResource.of(CoalGeneratorBlockEntity.this.getStack());
        }

        @Override
        public long getAmountAsLong(int index){
            return CoalGeneratorBlockEntity.this.getStack().getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource){
            ItemStack current = CoalGeneratorBlockEntity.this.getStack();
            return current.isEmpty() ? 64 : current.getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource){
            TransferPreconditions.checkNonEmpty(resource);
            return CoalGeneratorBlockEntity.this.isItemValid(resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction){
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack current = CoalGeneratorBlockEntity.this.getStack();
            if(!current.isEmpty() && !resource.matches(current))
                return 0;

            int inserted = Math.min(amount, current.getMaxStackSize() - current.getCount());
            if(inserted > 0){
                this.snapshotJournal.updateSnapshots(transaction);
                CoalGeneratorBlockEntity.this.setStack(resource.toStack(current.getCount() + inserted));
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction){
            return 0;
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

    public ResourceHandler<ItemResource> getItemCapability(){
        return this.itemCapability;
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
