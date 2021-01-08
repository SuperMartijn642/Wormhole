package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.*;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerTile extends PortalGroupTile implements ITargetCellTile, IEnergyCellTile {

    private final List<PortalTarget> targets = new ArrayList<>();
    private int energy = 0;

    public StabilizerTile(){
        super();
        for(int i = 0; i < this.getTargetCapacity(); i++)
            this.targets.add(null);
    }

    @Override
    public void update(){
        super.update();
        if(this.getBlockState().getBlock() instanceof StabilizerBlock && this.hasGroup() != this.getBlockState().getValue(StabilizerBlock.ON_PROPERTY))
            this.world.setBlockState(this.pos, Wormhole.portal_stabilizer.getDefaultState().withProperty(StabilizerBlock.ON_PROPERTY, this.hasGroup()), 2);
    }

    public boolean activate(EntityPlayer player){
        if(this.hasGroup()){
            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            if(!(stack.getItem() instanceof TargetDeviceItem))
                stack = player.getHeldItem(EnumHand.OFF_HAND);

            if(stack.getItem() instanceof TargetDeviceItem){
                if(this.world.isRemote)
                    ClientProxy.openPortalTargetScreen(this.pos);
            }else if(this.world.isRemote)
                ClientProxy.openPortalOverviewScreen(this.pos);
        }else if(!this.world.isRemote){
            PortalShape shape = PortalShape.find(this.world, this.pos);
            if(shape == null)
                player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.error").setStyle(new Style().setColor(TextFormatting.RED)));
            else{
                PortalGroupCapability groups = this.world.getCapability(PortalGroupCapability.CAPABILITY, null);
                if(groups != null)
                    groups.add(shape);
                player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.success").setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }
        }
        return true;
    }

    @Override
    public int getTargetCapacity(){
        return WormholeConfig.stabilizerTargetCapacity;
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

        return WormholeConfig.stabilizerEnergyCapacity;
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
    public <T> T getCapability(Capability<T> cap, EnumFacing side){
        if(cap == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(this);
        return super.getCapability(cap, side);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound tag = super.writeData();
        NBTTagCompound targetsTag = new NBTTagCompound();
        int count = 0;
        for(int i = 0; i < this.targets.size(); i++){
            targetsTag.setBoolean("has" + i, this.targets.get(i) != null);
            if(this.targets.get(i) != null){
                targetsTag.setTag("target" + i, this.targets.get(i).write());
                count = i + 1;
            }
        }
        tag.setInteger("targetCount", count);
        tag.setTag("targets", targetsTag);
        tag.setInteger("energy", this.energy);
        return tag;
    }

    @Override
    protected void readData(NBTTagCompound tag){
        super.readData(tag);
        this.targets.clear();
        int count = tag.hasKey("targetCount") ? tag.getInteger("targetCount") : 0;
        NBTTagCompound targetsTag = tag.getCompoundTag("targets");
        for(int i = 0; i < this.getTargetCapacity(); i++){
            if(i < count && targetsTag.hasKey("has" + i) && targetsTag.getBoolean("has" + i) && targetsTag.hasKey("target" + i))
                this.targets.add(new PortalTarget(targetsTag.getCompoundTag("target" + i)));
            else
                this.targets.add(null);
        }
        this.energy = tag.hasKey("energy") ? tag.getInteger("energy") : 0;

        if(tag.hasKey("group")){ // for older versions
            NBTTagCompound groupTag = new NBTTagCompound();
            if(groupTag.hasKey("target"))
                this.targets.set(0, PortalTarget.read(groupTag.getCompoundTag("target")));
        }
    }
}
