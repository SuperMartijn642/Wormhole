package com.supermartijn642.wormhole.portal;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.TeleportHelper;
import com.supermartijn642.wormhole.WormholeConfig;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 7/24/2020 by SuperMartijn642
 */
public class PortalGroup {

    public final PortalShape shape;
    public final World level;
    public boolean canTick = false;

    private int activeTarget = 0;
    private boolean activated;

    public PortalGroup(World level, PortalShape shape){
        this.level = level;
        this.shape = shape;
    }

    public PortalGroup(World level, CompoundNBT tag){
        this.level = level;
        this.shape = PortalShape.read(tag.getCompound("shape"));
        this.activeTarget = tag.contains("activeTarget") ? tag.getInt("activeTarget") : 0;
        this.activated = tag.contains("activated") && tag.getBoolean("activated");
    }

    public void tick(){
        if(!this.canTick)
            return;
        this.canTick = false;

        if(this.activated && WormholeConfig.requirePower.get()){
            if(this.getStoredEnergy() < this.getIdleEnergyCost())
                this.deactivate();
            else
                this.drainEnergy(this.getIdleEnergyCost());
        }
    }

    public void setTarget(int index, PortalTarget target){
        int total = 0;
        for(BlockPos pos : this.shape.targetCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof ITargetCellEntity){
                int capacity = ((ITargetCellEntity)entity).getTargetCapacity();
                if(total + capacity > index){
                    ((ITargetCellEntity)entity).setTarget(index - total, target);
                    break;
                }
                total += capacity;
            }
        }
        if(this.activated && index == this.activeTarget){
            if(target == null)
                this.deactivate();
            else
                this.shape.createPortals(this.level, target.color);
        }
    }

    public void clearTarget(int index){
        this.setTarget(index, null);
    }

    public void moveTarget(int index, boolean up){
        if(up ? index == 0 : index == this.getTotalTargetCapacity() - 1)
            return;

        int lowIndex = up ? index - 1 : index;
        int highIndex = up ? index : index + 1;

        ITargetCellEntity lowEntity = null;
        int lowEntityIndex = 0;
        ITargetCellEntity highEntity = null;
        int highEntityIndex = 0;

        int total = 0;
        for(BlockPos pos : this.shape.targetCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof ITargetCellEntity){
                int capacity = ((ITargetCellEntity)entity).getTargetCapacity();
                if(lowEntity == null && total + capacity > lowIndex){
                    lowEntity = (ITargetCellEntity)entity;
                    lowEntityIndex = lowIndex - total;
                }
                if(highEntity == null && total + capacity > highIndex){
                    highEntity = (ITargetCellEntity)entity;
                    highEntityIndex = highIndex - total;
                }
                total += capacity;
            }
        }

        if(lowEntity == null || highEntity == null)
            return;

        PortalTarget lowTarget = lowEntity.getTarget(lowEntityIndex);
        lowEntity.setTarget(lowEntityIndex, highEntity.getTarget(highEntityIndex));
        highEntity.setTarget(highEntityIndex, lowTarget);

        if(lowIndex == this.activeTarget){
            this.activeTarget++;
            this.updateGroup();
        }else if(highIndex == this.activeTarget){
            this.activeTarget--;
            this.updateGroup();
        }
    }

    public void setActiveTarget(int index){
        if(index != this.activeTarget){
            this.activeTarget = index;
            if(this.activated){
                PortalTarget target = this.getActiveTarget();
                if(target == null)
                    deactivate();
                else
                    this.shape.createPortals(this.level, this.getActiveTarget().color);
            }
            this.updateGroup();
        }
    }

    public PortalTarget getActiveTarget(){
        return this.getTarget(this.activeTarget);
    }

    public int getActiveTargetIndex(){
        return this.activeTarget;
    }

    public int getTotalTargetCapacity(){
        int total = 0;
        for(BlockPos pos : this.shape.targetCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof ITargetCellEntity)
                total += ((ITargetCellEntity)entity).getTargetCapacity();
        }
        return total;
    }

    public PortalTarget getTarget(int index){
        if(index < 0)
            return null;

        int total = 0;
        for(BlockPos pos : this.shape.targetCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof ITargetCellEntity){
                int capacity = ((ITargetCellEntity)entity).getTargetCapacity();
                if(total + capacity > index)
                    return ((ITargetCellEntity)entity).getTarget(index - total);
                total += capacity;
            }
        }
        return null;
    }

    public boolean hasTargetSpaceLeft(){
        for(int i = 0; i < this.getTotalTargetCapacity(); i++){
            if(this.getTarget(i) == null)
                return true;
        }
        return false;
    }

    public void addTarget(PortalTarget target){
        for(int i = 0; i < this.getTotalTargetCapacity(); i++){
            if(this.getTarget(i) == null){
                this.setTarget(i, target);
                return;
            }
        }
    }

    public int getEnergyCapacity(){
        int total = 0;
        for(BlockPos pos : this.shape.energyCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof IEnergyStorage)
                total += ((IEnergyCellEntity)entity).getMaxEnergyStored(true);
        }
        return total;
    }

    public int getStoredEnergy(){
        int total = 0;
        for(BlockPos pos : this.shape.energyCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof IEnergyStorage)
                total += ((IEnergyCellEntity)entity).getEnergyStored(true);
        }
        return total;
    }

    public void drainEnergy(int energy){
        for(BlockPos pos : this.shape.energyCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof IEnergyStorage)
                energy -= ((IEnergyCellEntity)entity).extractEnergy(energy, false, true);
        }
    }

    public int receiveEnergy(int energy, boolean simulate){
        int received = 0;
        for(BlockPos pos : this.shape.energyCells){
            TileEntity entity = this.level.getBlockEntity(pos);
            if(entity instanceof IEnergyStorage){
                received += ((IEnergyCellEntity)entity).receiveEnergy(energy - received, simulate, true);
                if(received >= energy)
                    break;
            }
        }
        return received;
    }

    public void activate(){
        if(!this.activated && this.getActiveTarget() != null && this.shape.validatePortal(this.level)
            && (!WormholeConfig.requirePower.get() || this.getStoredEnergy() >= this.getIdleEnergyCost())){
            this.shape.createPortals(this.level, this.getActiveTarget().color);
            this.activated = true;
            this.updateGroup();
        }
    }

    public void deactivate(){
        if(this.activated){
            this.shape.destroyPortals(this.level);
            this.activated = false;
            this.updateGroup();
        }
    }

    public boolean isActive(){
        return activated;
    }

    public void teleport(Entity entity){
        PortalTarget target = this.getActiveTarget();
        if(!this.activated || target == null || !TeleportHelper.canTeleport(entity, target))
            return;

        if(WormholeConfig.requirePower.get()){
            int energy = this.getStoredEnergy();
            int cost = this.getTeleportEnergyCost();
            if(energy < cost){
                this.drainEnergy(energy);
                return;
            }
            this.drainEnergy(cost);
        }

        TeleportHelper.queTeleport(entity, target);
    }

    public int getTeleportEnergyCost(){
        PortalTarget target = this.getActiveTarget();
        if(target == null)
            return 0;
        return getTeleportCostToTarget(this.level, this.getCenterPos(), target);
    }

    public int getIdleEnergyCost(){
        return WormholeConfig.idlePowerDrain.get() + (int)Math.round(this.shape.area.size() * WormholeConfig.sizePowerDrain.get());
    }

    public void destroy(){
        this.deactivate();
        this.level.getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.remove(this));
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.put("shape", this.shape.write());
        tag.putInt("activeTarget", this.activeTarget);
        tag.putBoolean("activated", this.activated);
        return tag;
    }

    private void updateGroup(){
        this.level.getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.updateGroup(this));
    }

    public BlockPos getCenterPos(){
        return new BlockPos(
            (this.shape.minCorner.getX() + this.shape.maxCorner.getX()) / 2,
            (this.shape.minCorner.getY() + this.shape.maxCorner.getY()) / 2,
            (this.shape.minCorner.getZ() + this.shape.maxCorner.getZ()) / 2
        );
    }

    public static int getTeleportCostToTarget(World world, BlockPos portalCenter, PortalTarget target){
        return WormholeConfig.travelPowerDrain.get() +
            (target.dimension.equals(world.getDimension().getType()) ?
                (int)Math.round(Math.pow(portalCenter.distSqr(target.getPos()), 1 / 4d) * WormholeConfig.distancePowerDrain.get()) :
                WormholeConfig.dimensionPowerDrain.get());
    }
}
