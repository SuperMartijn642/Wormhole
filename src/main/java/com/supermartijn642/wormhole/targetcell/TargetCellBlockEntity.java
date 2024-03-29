package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.ITargetCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlockEntity extends PortalGroupBlockEntity implements ITargetCellEntity {

    public static class BasicTargetCellBlockEntity extends TargetCellBlockEntity {

        public BasicTargetCellBlockEntity(){
            super(TargetCellType.BASIC);
        }
    }

    public static class AdvancedTargetCellBlockEntity extends TargetCellBlockEntity {

        public AdvancedTargetCellBlockEntity(){
            super(TargetCellType.ADVANCED);
        }
    }

    public final TargetCellType type;
    private final List<PortalTarget> targets = new ArrayList<>();
    private int ticks = 20;

    public TargetCellBlockEntity(TargetCellType type){
        super(type.getBlockEntityType());
        this.type = type;

        for(int i = 0; i < type.getCapacity(); i++)
            this.targets.add(null);
    }

    @Override
    public void update(){
        super.update();
        // Update block state
        this.ticks++;
        if(this.ticks >= 20){
            int targetCount = Math.min(this.getNonNullTargetCount(), this.type.getVisualCapacity());
            if(this.getBlockState().getValue(TargetCellBlock.VISUAL_TARGETS) != targetCount){
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(TargetCellBlock.VISUAL_TARGETS, targetCount));
                this.ticks = 0;
            }
        }
    }

    @Override
    public int getTargetCapacity(){
        return this.type.getCapacity();
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
    protected CompoundNBT writeData(){
        CompoundNBT tag = super.writeData();
        CompoundNBT targetsTag = new CompoundNBT();
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
        return tag;
    }

    @Override
    protected void readData(CompoundNBT tag){
        super.readData(tag);
        this.targets.clear();
        int count = tag.contains("targetCount") ? tag.getInt("targetCount") : 0;
        CompoundNBT targetsTag = tag.getCompound("targets");
        for(int i = 0; i < this.getTargetCapacity(); i++){
            if(i < count && targetsTag.contains("has" + i) && targetsTag.getBoolean("has" + i) && targetsTag.contains("target" + i))
                this.targets.add(new PortalTarget(targetsTag.getCompound("target" + i)));
            else
                this.targets.add(null);
        }
    }
}
