package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetPassengersPacket;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class TeleportHelper {

    private static final Field field_242271_ac = ObfuscationReflectionHelper.findField(Entity.class, "field_242271_ac");

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public static boolean queTeleport(Entity entity, PortalTarget target){
        if(!canTeleport(entity, target))
            return false;
        for(Entity passenger : entity.getRecursivePassengers())
            if(passenger instanceof PlayerEntity)
                return false;

        Entity lowestEntity = entity.getLowestRidingEntity();
        if(!entity.world.isRemote){
            lowestEntity.world.getServer().enqueue(new TickDelayedTask(0, () -> teleportEntityAndPassengers(lowestEntity, null, target)));
            markEntityAndPassengers(lowestEntity);
        }
        return true;
    }

    public static boolean canTeleport(Entity entity, PortalTarget target){
        if(entity.world.isRemote || !target.getWorld(entity.getServer()).isPresent())
            return false;
        if(entity.isPassenger())
            return canTeleport(entity.getLowestRidingEntity(), target);

        for(Entity rider : entity.getRecursivePassengers()){
            CompoundNBT tag = rider.getPersistentData();
            if(tag.contains("wormhole:teleported") && rider.ticksExisted - tag.getLong("wormhole:teleported") >= 0 && rider.ticksExisted - tag.getLong("wormhole:teleported") < TELEPORT_COOLDOWN)
                return false;
        }

        CompoundNBT tag = entity.getPersistentData();
        return !tag.contains("wormhole:teleported") || entity.ticksExisted - tag.getLong("wormhole:teleported") < 0 || entity.ticksExisted - tag.getLong("wormhole:teleported") >= TELEPORT_COOLDOWN;
    }

    private static void markEntityAndPassengers(Entity entity){
        entity.getPersistentData().putLong("wormhole:teleported", entity.ticksExisted);
        entity.getPassengers().forEach(TeleportHelper::markEntityAndPassengers);
    }

    private static void teleportEntityAndPassengers(Entity entity, Entity entityBeingRidden, PortalTarget target){
        if(entity.world.isRemote || !target.getWorld(entity.getServer()).isPresent())
            return;
        Optional<ServerWorld> targetWorld = target.getWorld(entity.getServer()).filter(ServerWorld.class::isInstance).map(ServerWorld.class::cast);
        if(!targetWorld.isPresent())
            return;

        Collection<Entity> passengers = entity.getPassengers();
        entity.removePassengers();
        Entity newEntity = teleportEntity(entity, targetWorld.get(), target);
        if(entityBeingRidden != null){
            newEntity.startRiding(entityBeingRidden);
            if(newEntity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)newEntity).connection.sendPacket(new SSetPassengersPacket(entityBeingRidden));
        }
        passengers.forEach(e -> teleportEntityAndPassengers(e, newEntity, target));
    }

    private static Entity teleportEntity(Entity entity, ServerWorld targetWorld, PortalTarget target){
        if(targetWorld == entity.world){
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).teleport(targetWorld, target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.setPositionAndUpdate(target.x + .5, target.y + .2, target.z + .5);
            entity.setRotationYawHead(target.yaw);
            entity.setMotion(Vector3d.ZERO);
            entity.fallDistance = 0;
            entity.setOnGround(true);
        }else{
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).teleport(targetWorld, target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else{
                try{
                    field_242271_ac.setAccessible(true);
                    field_242271_ac.set(entity, BlockPos.ZERO);
                }catch(IllegalAccessException e){
                    return entity;
                }
                Entity newEntity = entity.changeDimension(targetWorld, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean,Entity> repositionEntity){
                        Entity newEntity = entity.getType().create(targetWorld);
                        if(newEntity != null){
                            newEntity.copyDataFromOld(entity);
                            newEntity.setLocationAndAngles(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
                            newEntity.setMotion(Vector3d.ZERO);
                            targetWorld.addFromAnotherDimension(newEntity);
                        }
                        return newEntity;
                    }
                });
                if(newEntity != null)
                    return newEntity;
            }
        }
        return entity;
    }
}
