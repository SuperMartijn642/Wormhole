package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.ITargetCellTile;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellTile extends PortalGroupTile implements ITargetCellTile {

    public static class BasicTargetCellTile extends TargetCellTile {
        public BasicTargetCellTile(){
            super(TargetCellType.BASIC);
        }
    }

    public static class AdvancedTargetCellTile extends TargetCellTile {
        public AdvancedTargetCellTile(){
            super(TargetCellType.ADVANCED);
        }
    }

    public final TargetCellType type;
    private final List<PortalTarget> targets = new ArrayList<>();

    public TargetCellTile(TargetCellType type){
        super();
        this.type = type;

        for(int i = 0; i < type.getCapacity(); i++)
            this.targets.add(null);
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
    }
}
