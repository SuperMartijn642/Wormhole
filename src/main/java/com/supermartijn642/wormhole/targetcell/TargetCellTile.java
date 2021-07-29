package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.ITargetCellTile;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellTile extends PortalGroupTile implements ITargetCellTile {

    public static class BasicTargetCellTile extends TargetCellTile {

        public BasicTargetCellTile(BlockPos pos, BlockState state){
            super(TargetCellType.BASIC, pos, state);
        }
    }

    public static class AdvancedTargetCellTile extends TargetCellTile {

        public AdvancedTargetCellTile(BlockPos pos, BlockState state){
            super(TargetCellType.ADVANCED, pos, state);
        }
    }

    public final TargetCellType type;
    private final List<PortalTarget> targets = new ArrayList<>();

    public TargetCellTile(TargetCellType type, BlockPos pos, BlockState state){
        super(type.getTileEntityType(), pos, state);
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
    }
}
