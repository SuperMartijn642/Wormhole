package com.supermartijn642.wormhole;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.wormhole.portal.*;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlockEntity extends PortalGroupBlockEntity implements ITargetCellEntity, IEnergyCellEntity {

    private final List<PortalTarget> targets = new ArrayList<>();
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this);
    private int energy = 0;

    public StabilizerBlockEntity(BlockPos pos, BlockState state){
        super(Wormhole.stabilizer_tile, pos, state);
        for(int i = 0; i < this.getTargetCapacity(); i++)
            this.targets.add(null);
    }

    @Override
    public void update(){
        super.update();
        if(this.getBlockState().getBlock() instanceof StabilizerBlock && this.hasGroup() != this.getBlockState().getValue(StabilizerBlock.ON_PROPERTY))
            this.level.setBlock(this.worldPosition, Wormhole.portal_stabilizer.defaultBlockState().setValue(StabilizerBlock.ON_PROPERTY, this.hasGroup()), 2);
    }

    public boolean activate(Player player){
        if(this.hasGroup()){
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if(!(stack.getItem() instanceof TargetDeviceItem))
                stack = player.getItemInHand(InteractionHand.OFF_HAND);

            if(stack.getItem() instanceof TargetDeviceItem){
                if(this.level.isClientSide)
                    WormholeClient.openPortalTargetScreen(this.worldPosition);
            }else if(this.level.isClientSide)
                WormholeClient.openPortalOverviewScreen(this.worldPosition);
        }else if(!this.level.isClientSide){
            PortalShape shape = PortalShape.find(this.level, this.worldPosition);
            if(shape == null)
                player.displayClientMessage(TextComponents.translation("wormhole.portal_stabilizer.error").color(ChatFormatting.RED).get(), true);
            else{
                this.level.getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.add(shape));
                player.displayClientMessage(TextComponents.translation("wormhole.portal_stabilizer.success").color(ChatFormatting.YELLOW).get(), true);
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
    public int receiveEnergy(int maxReceive, boolean simulate, boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().receiveEnergy(maxReceive, simulate);

        if(maxReceive < 0)
            return -this.extractEnergy(-maxReceive, simulate);
        int absorb = Math.min(this.getMaxEnergyStored(true) - this.energy, maxReceive);
        if(!simulate){
            this.energy += absorb;
            if(absorb > 0)
                this.dataChanged();
        }
        return absorb;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate, boolean fromGroup){
        if(maxExtract < 0)
            return -this.receiveEnergy(-maxExtract, simulate);
        int drain = Math.min(this.energy, maxExtract);
        if(!simulate){
            this.energy -= drain;
            if(drain > 0)
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
    public int getMaxEnergyStored(boolean fromGroup){
        if(!fromGroup && this.hasGroup())
            return this.getGroup().getEnergyCapacity();

        return WormholeConfig.stabilizerEnergyCapacity.get();
    }

    @Override
    public boolean canExtract(){
        return false;
    }

    @Override
    public boolean canReceive(){
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
        //noinspection removal
        if(cap == CapabilityEnergy.ENERGY)
            return this.energyCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps(){
        super.invalidateCaps();
        this.energyCapability.invalidate();
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag tag = super.writeData();
        CompoundTag targetsTag = new CompoundTag();
        int count = 0;
        for(int i = 0; i < this.targets.size(); i++){
            targetsTag.putBoolean("has" + i, this.targets.get(i) != null);
            if(this.targets.get(i) != null){
                targetsTag.put("target" + i, this.targets.get(i).write());
                count = i + 1;
            }
        }
        tag.putInt("targetCount", count);
        tag.put("targets", targetsTag);
        tag.putInt("energy", this.energy);
        return tag;
    }

    @Override
    protected void readData(CompoundTag tag){
        super.readData(tag);
        this.targets.clear();
        int count = tag.contains("targetCount") ? tag.getInt("targetCount") : 0;
        CompoundTag targetsTag = tag.getCompound("targets");
        for(int i = 0; i < this.getTargetCapacity(); i++){
            if(i < count && targetsTag.contains("has" + i) && targetsTag.getBoolean("has" + i) && targetsTag.contains("target" + i))
                this.targets.add(new PortalTarget(targetsTag.getCompound("target" + i)));
            else
                this.targets.add(null);
        }
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;

        if(tag.contains("group")){ // for older versions
            CompoundTag groupTag = new CompoundTag();
            if(groupTag.contains("target"))
                this.targets.set(0, PortalTarget.read(groupTag.getCompound("target")));
        }
    }
}
