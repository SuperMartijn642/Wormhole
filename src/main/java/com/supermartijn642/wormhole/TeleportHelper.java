package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Optional;
import java.util.function.Function;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class TeleportHelper {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public static void queTeleport(Entity entity, PortalTarget target){
        if(entity.world.isRemote)
            return;

        entity.world.getServer().enqueue(new TickDelayedTask(0, () -> teleport(entity, target)));
    }

    public static void teleport(Entity entity, PortalTarget target){
        if(!(entity.world instanceof ServerWorld) || !target.getWorld(entity.getServer()).isPresent())
            return;

        CompoundNBT tag = entity.getPersistentData();
        if(tag.contains("wormhole:teleported") && entity.ticksExisted - tag.getLong("wormhole:teleported") > 0 && entity.ticksExisted - tag.getLong("wormhole:teleported") < TELEPORT_COOLDOWN)
            return;

        Optional<ServerWorld> optionalTargetWorld = target.getWorld(entity.getServer()).filter(ServerWorld.class::isInstance).map(ServerWorld.class::cast);
        if(!optionalTargetWorld.isPresent())
            return;

        ServerWorld targetWorld = optionalTargetWorld.get();

        if(targetWorld == entity.world){
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).connection.setPlayerLocation(target.x + .5, target.y, target.z + .5, target.yaw, 0);
            else{
                entity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                entity.setRotationYawHead(target.yaw);
            }
            entity.setMotion(Vec3d.ZERO);
            entity.fallDistance = 0;
            entity.onGround = true;
        }else{
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).teleport(targetWorld, target.x, target.y, target.z, target.yaw, 0);
            else{
                entity.changeDimension(targetWorld.getDimension().getType(), new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean,Entity> repositionEntity){
                        Entity newEntity = entity.getType().create(targetWorld);
                        if(newEntity != null){
                            newEntity.copyDataFromOld(entity);
                            newEntity.setLocationAndAngles(target.x, target.y, target.z, target.yaw, 0);
                            newEntity.setMotion(Vec3d.ZERO);
                            targetWorld.func_217460_e(newEntity);
                        }
                        return newEntity;
                    }
                });
            }
        }

        tag.putLong("wormhole:teleported", entity.ticksExisted);
    }
}
