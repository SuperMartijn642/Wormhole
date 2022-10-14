package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Collection;
import java.util.Optional;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class TeleportHelper {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public static boolean queTeleport(Entity entity, PortalTarget target){
        if(!canTeleport(entity, target))
            return false;
        for(Entity passenger : entity.getIndirectPassengers())
            if(passenger instanceof PlayerEntity)
                return false;

        Entity lowestEntity = entity.getRootVehicle();
        if(!entity.level.isClientSide){
            lowestEntity.level.getServer().tell(new TickDelayedTask(0, () -> teleportEntityAndPassengers(lowestEntity, null, target)));
            markEntityAndPassengers(lowestEntity);
        }
        return true;
    }

    public static boolean canTeleport(Entity entity, PortalTarget target){
        if(entity.level.isClientSide || !target.getLevel(entity.getServer()).isPresent())
            return false;
        if(entity.isPassenger())
            return canTeleport(entity.getRootVehicle(), target);

        for(Entity rider : entity.getIndirectPassengers()){
            CompoundNBT tag = rider.getPersistentData();
            if(tag.contains("wormhole:teleported") && rider.tickCount - tag.getLong("wormhole:teleported") >= 0 && rider.tickCount - tag.getLong("wormhole:teleported") < TELEPORT_COOLDOWN)
                return false;
        }

        CompoundNBT tag = entity.getPersistentData();
        return !tag.contains("wormhole:teleported") || entity.tickCount - tag.getLong("wormhole:teleported") < 0 || entity.tickCount - tag.getLong("wormhole:teleported") >= TELEPORT_COOLDOWN;
    }

    private static void markEntityAndPassengers(Entity entity){
        entity.getPersistentData().putLong("wormhole:teleported", entity.tickCount);
        entity.getPassengers().forEach(TeleportHelper::markEntityAndPassengers);
    }

    private static void teleportEntityAndPassengers(Entity entity, Entity entityBeingRidden, PortalTarget target){
        if(entity.level.isClientSide || !target.getLevel(entity.getServer()).isPresent())
            return;
        Optional<ServerWorld> targetLevel = target.getLevel(entity.getServer()).filter(ServerWorld.class::isInstance).map(ServerWorld.class::cast);
        if(!targetLevel.isPresent())
            return;

        Collection<Entity> passengers = entity.getPassengers();
        entity.ejectPassengers();
        Entity newEntity = teleportEntity(entity, targetLevel.get(), target);
        if(entityBeingRidden != null){
            newEntity.startRiding(entityBeingRidden);
            if(newEntity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)newEntity).connection.send(new SSetPassengersPacket(entityBeingRidden));
        }
        passengers.forEach(e -> teleportEntityAndPassengers(e, newEntity, target));
    }

    private static Entity teleportEntity(Entity entity, ServerWorld targetLevel, PortalTarget target){
        if(targetLevel == entity.level){
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).connection.teleport(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.teleportTo(target.x + .5, target.y + .2, target.z + .5);
            entity.setYHeadRot(target.yaw);
            entity.setDeltaMovement(Vec3d.ZERO);
            entity.fallDistance = 0;
            entity.onGround = true;
        }else if(net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity, targetLevel.dimension.getType()) && !entity.removed){
            if(entity instanceof ServerPlayerEntity){
                ServerPlayerEntity player = (ServerPlayerEntity)entity;
                DimensionType oldDimension = entity.dimension;

                ServerWorld oldLevel = player.server.getLevel(oldDimension);
                entity.dimension = targetLevel.dimension.getType();
                WorldInfo levelData = entity.level.getLevelData();
                NetworkHooks.sendDimensionDataPacket(player.connection.connection, player);
                player.connection.send(new SRespawnPacket(targetLevel.dimension.getType(), levelData.getGeneratorType(), player.gameMode.getGameModeForPlayer()));
                player.connection.send(new SServerDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
                PlayerList playerList = player.server.getPlayerList();
                playerList.sendPlayerPermissionLevel(player);
                oldLevel.removeEntity(player, true); //Forge: the player entity is moved to the new world, NOT cloned. So keep the data alive with no matching invalidate call.
                player.revive();

                player.moveTo(target.x, target.y, target.z, target.yaw, 0);
                player.setDeltaMovement(Vec3d.ZERO);

                player.setLevel(targetLevel);
                targetLevel.addDuringPortalTeleport(player);
                CriteriaTriggers.CHANGED_DIMENSION.trigger(player, oldDimension, targetLevel.dimension.getType());
                player.connection.teleport(player.x, player.y, player.z, player.yRot, player.xRot);
                player.gameMode.setLevel(targetLevel);
                player.connection.send(new SPlayerAbilitiesPacket(player.abilities));
                playerList.sendLevelInfo(player, targetLevel);
                playerList.sendAllPlayerInfo(player);

                for(EffectInstance effectinstance : player.getActiveEffects())
                    player.connection.send(new SPlayEntityEffectPacket(player.getId(), effectinstance));

                player.connection.send(new SPlaySoundEventPacket(1032, BlockPos.ZERO, 0, false));
                player.setExperienceLevels(player.experienceLevel);
                BasicEventHooks.firePlayerChangedDimensionEvent(player, oldDimension, targetLevel.dimension.getType());
            }else{
                MinecraftServer server = entity.getServer();
                ServerWorld oldLevel = server.getLevel(entity.dimension);
                entity.dimension = targetLevel.dimension.getType();
                entity.unRide();

                Entity newEntity = entity.getType().create(targetLevel);
                if(newEntity != null){
                    newEntity.restoreFrom(entity);
                    newEntity.moveTo(target.getPos(), target.yaw, 0);
                    newEntity.setDeltaMovement(Vec3d.ZERO);
                    targetLevel.addFromAnotherDimension(newEntity);
                }

                entity.remove(false);
                oldLevel.resetEmptyTime();
                targetLevel.resetEmptyTime();
                return newEntity;
            }
        }
        return entity;
    }
}
