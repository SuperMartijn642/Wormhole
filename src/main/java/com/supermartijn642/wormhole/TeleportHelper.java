package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Optional;
import java.util.function.Function;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class TeleportHelper {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public static boolean queTeleport(Entity entity, PortalTarget target){
        if(!canTeleport(entity, target))
            return false;

        entity.world.getServer().enqueue(new TickDelayedTask(0, () -> teleportEntity(entity, target)));

        entity.getPersistentData().putLong("wormhole:teleported", entity.ticksExisted);
        return true;
    }

    public static boolean teleport(Entity entity, PortalTarget target){
        if(!canTeleport(entity, target))
            return false;

        teleportEntity(entity, target);

        entity.getPersistentData().putLong("wormhole:teleported", entity.ticksExisted);
        return true;
    }

    public static boolean canTeleport(Entity entity, PortalTarget target){
        if(entity.world.isRemote || !target.getWorld(entity.getServer()).isPresent())
            return false;

        CompoundNBT tag = entity.getPersistentData();
        return !tag.contains("wormhole:teleported") || entity.ticksExisted - tag.getLong("wormhole:teleported") < 0 || entity.ticksExisted - tag.getLong("wormhole:teleported") >= TELEPORT_COOLDOWN;
    }

    private static void teleportEntity(Entity entity, PortalTarget target){
        if(entity.world.isRemote || !target.getWorld(entity.getServer()).isPresent())
            return;

        Optional<ServerWorld> optionalTargetWorld = target.getWorld(entity.getServer()).filter(ServerWorld.class::isInstance).map(ServerWorld.class::cast);
        if(!optionalTargetWorld.isPresent())
            return;

        ServerWorld targetWorld = optionalTargetWorld.get();

        if(targetWorld == entity.world){
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).connection.setPlayerLocation(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.setLocationAndAngles(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            entity.setRotationYawHead(target.yaw);
            entity.setMotion(Vector3d.ZERO);
            entity.fallDistance = 0;
            entity.setOnGround(true);
        }else{
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).teleport(targetWorld, target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else{
                entity.changeDimension(optionalTargetWorld.get(), new ITeleporter() {
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
            }
        }
    }
}
