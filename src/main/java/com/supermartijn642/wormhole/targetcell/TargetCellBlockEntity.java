package com.supermartijn642.wormhole.targetcell;

import com.supermartijn642.wormhole.portal.ITargetCellEntity;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/16/2020 by SuperMartijn642
 */
public class TargetCellBlockEntity extends PortalGroupBlockEntity implements ITargetCellEntity {

    public static class BasicTargetCellBlockEntity extends TargetCellBlockEntity {

        public BasicTargetCellBlockEntity(BlockPos pos, BlockState state){
            super(TargetCellType.BASIC, pos, state);
        }
    }

    public static class AdvancedTargetCellBlockEntity extends TargetCellBlockEntity {

        public AdvancedTargetCellBlockEntity(BlockPos pos, BlockState state){
            super(TargetCellType.ADVANCED, pos, state);
        }
    }

    public final TargetCellType type;
    private final List<PortalTarget> targets = new ArrayList<>();
    private int ticks = 20;

    public TargetCellBlockEntity(TargetCellType type, BlockPos pos, BlockState state){
        super(type.getBlockEntityType(), pos, state);
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
    protected void writeData(ValueOutput output){
        super.writeData(output);
        ValueOutput targetsTag = output.child("targets");
        for(int i = 0; i < this.targets.size(); i++){
            if(this.targets.get(i) != null)
                this.targets.get(i).write(targetsTag.child("target" + i));
        }
    }

    @Override
    protected void readData(ValueInput input){
        super.readData(input);
        this.targets.clear();
        ValueInput targetsTag = input.childOrEmpty("targets");
        for(int i = 0; i < this.getTargetCapacity(); i++)
            this.targets.add(targetsTag.child("target" + i).map(PortalTarget::read).orElse(null));
    }
}
