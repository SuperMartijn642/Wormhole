package com.supermartijn642.wormhole;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.energycell.EnergyCellEnergyHandlerWrapper;
import com.supermartijn642.wormhole.portal.*;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlockEntity extends PortalGroupBlockEntity implements ITargetCellEntity, IEnergyCellEntity {

    private final SnapshotJournal<Integer> snapshotJournal = new SnapshotJournal<>() {
        @Override
        protected Integer createSnapshot(){
            return StabilizerBlockEntity.this.energy;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot){
            StabilizerBlockEntity.this.energy = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState){
            if(originalState != StabilizerBlockEntity.this.energy)
                StabilizerBlockEntity.this.dataChanged();
        }
    };
    public final EnergyHandler energyHandler = new EnergyCellEnergyHandlerWrapper(this);

    private final List<PortalTarget> targets = new ArrayList<>();
    private int energy = 0;

    public StabilizerBlockEntity(BlockPos pos, BlockState state){
        super(Wormhole.stabilizer_tile, pos, state);
        for(int i = 0; i < this.getTargetCapacity(); i++)
            this.targets.add(null);
    }

    @Override
    public void update(){
        super.update();
        if(!this.level.isClientSide() && this.getBlockState().getBlock() instanceof StabilizerBlock && this.hasGroup() != this.getBlockState().getValue(StabilizerBlock.ON_PROPERTY))
            this.level.setBlock(this.worldPosition, Wormhole.portal_stabilizer.defaultBlockState().setValue(StabilizerBlock.ON_PROPERTY, this.hasGroup()), 2);
    }

    public boolean activate(Player player){
        if(this.hasGroup()){
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if(!(stack.getItem() instanceof TargetDeviceItem))
                stack = player.getItemInHand(InteractionHand.OFF_HAND);

            if(stack.getItem() instanceof TargetDeviceItem){
                if(this.level.isClientSide())
                    WormholeClient.openPortalTargetScreen(this.worldPosition);
            }else if(this.level.isClientSide())
                WormholeClient.openPortalOverviewScreen(this.worldPosition);
        }else if(!this.level.isClientSide()){
            PortalShape shape = PortalShape.find(this.level, this.worldPosition);
            if(shape == null)
                player.sendOverlayMessage(TextComponents.translation("wormhole.portal_stabilizer.error").color(ChatFormatting.RED).get());
            else{
                PortalGroupCapability.get(this.level).add(shape);
                player.sendOverlayMessage(TextComponents.translation("wormhole.portal_stabilizer.success").color(ChatFormatting.YELLOW).get());
            }
        }
        return true;
    }

    @Override
    public int getTargetCapacity(){
        return WormholeConfig.stabilizerTargetCapacity.get();
    }

    @Override
    public PortalTarget getTarget(int index){
        return this.targets.get(index);
    }

    @Override
    public void setTarget(int index, PortalTarget target){
        this.targets.set(index, target);
        this.dataChanged();
    }

    @Override
    public List<PortalTarget> getTargets(){
        return this.targets;
    }

    @Override
    public int getNonNullTargetCount(){
        int count = 0;
        for(PortalTarget target : this.targets)
            if(target != null)
                count++;
        return count;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean fromGroup, TransactionContext transaction){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().receiveEnergy(maxReceive, transaction);

        int absorb = Math.min(this.getMaxEnergyStored(true) - this.energy, maxReceive);
        if(absorb > 0){
            this.snapshotJournal.updateSnapshots(transaction);
            this.energy += absorb;
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean fromGroup, TransactionContext transaction){
        if(!fromGroup)
            return 0;
        int drain = Math.min(this.energy, maxExtract);
        if(drain > 0){
            if(transaction != null)
                this.snapshotJournal.updateSnapshots(transaction);
            this.energy -= drain;
            if(transaction == null)
                this.dataChanged();
        }
        return drain;
    }

    @Override
    public int getEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getStoredEnergy();

        return Math.min(this.energy, this.getMaxEnergyStored(true));
    }

    @Override
    public void setEnergyStored(int energy){
        this.energy = energy;
    }

    @Override
    public int getMaxEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getEnergyCapacity();

        return WormholeConfig.stabilizerEnergyCapacity.get();
    }

    @Override
    protected void writeData(ValueOutput output){
        super.writeData(output);
        ValueOutput targetsTag = output.child("targets");
        for(int i = 0; i < this.targets.size(); i++){
            if(this.targets.get(i) != null)
                this.targets.get(i).write(targetsTag.child("target" + i));
        }
        output.putInt("energy", this.energy);
    }

    @Override
    protected void readData(ValueInput input){
        super.readData(input);
        this.targets.clear();
        ValueInput targetsTag = input.childOrEmpty("targets");
        for(int i = 0; i < this.getTargetCapacity(); i++)
            this.targets.add(targetsTag.child("target" + i).map(PortalTarget::read).orElse(null));
        this.energy = input.getIntOr("energy", 0);
    }
}
