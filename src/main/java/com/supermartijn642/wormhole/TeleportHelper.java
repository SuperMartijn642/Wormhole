package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

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
            if(passenger instanceof Player)
                return false;

        Entity lowestEntity = entity.getRootVehicle();
        if(!entity.level().isClientSide){
            lowestEntity.level().getServer().tell(new TickTask(0, () -> teleportEntityAndPassengers(lowestEntity, null, target)));
            markEntityAndPassengers(lowestEntity);
        }
        return true;
    }

    public static boolean canTeleport(Entity entity, PortalTarget target){
        if(entity.level().isClientSide || !target.getLevel(entity.getServer()).isPresent())
            return false;
        if(entity.isPassenger())
            return canTeleport(entity.getRootVehicle(), target);

        for(Entity rider : entity.getIndirectPassengers()){
            if(rider.isOnPortalCooldown())
                return false;
        }

        return !entity.isOnPortalCooldown();
    }

    private static void markEntityAndPassengers(Entity entity){
        entity.portalCooldown = TELEPORT_COOLDOWN;
        entity.getPassengers().forEach(TeleportHelper::markEntityAndPassengers);
    }

    private static void teleportEntityAndPassengers(Entity entity, Entity entityBeingRidden, PortalTarget target){
        if(entity.level().isClientSide || !target.getLevel(entity.getServer()).isPresent())
            return;
        Optional<ServerLevel> targetLevel = target.getLevel(entity.getServer()).filter(ServerLevel.class::isInstance).map(ServerLevel.class::cast);
        if(!targetLevel.isPresent())
            return;

        Collection<Entity> passengers = entity.getPassengers();
        entity.ejectPassengers();
        Entity newEntity = teleportEntity(entity, targetLevel.get(), target);
        if(entityBeingRidden != null){
            newEntity.startRiding(entityBeingRidden);
            if(newEntity instanceof ServerPlayer)
                ((ServerPlayer)newEntity).connection.send(new ClientboundSetPassengersPacket(entityBeingRidden));
        }
        passengers.forEach(e -> teleportEntityAndPassengers(e, newEntity, target));
    }

    private static Entity teleportEntity(Entity entity, ServerLevel targetLevel, PortalTarget target){
        ChunkPos targetChunkPos = new ChunkPos(target.getPos());
        targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, entity.getId());
        if(targetLevel == entity.level()){
            if(entity instanceof ServerPlayer)
                ((ServerPlayer)entity).teleportTo(targetLevel, target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.teleportTo(target.x + .5, target.y + .2, target.z + .5);
            entity.setYHeadRot(target.yaw);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0;
            entity.setOnGround(true);
        }else{
            if(entity instanceof ServerPlayer){
                ServerPlayer player = ((ServerPlayer)entity);
                player.isChangingDimension = true;
                LevelData levelData = targetLevel.getLevelData();
                player.connection.send(new ClientboundRespawnPacket(new CommonPlayerSpawnInfo(targetLevel.dimensionTypeId(), targetLevel.dimension(), BiomeManager.obfuscateSeed(targetLevel.getSeed()), player.gameMode.getGameModeForPlayer(), player.gameMode.getPreviousGameModeForPlayer(), targetLevel.isDebug(), targetLevel.isFlat(), player.getLastDeathLocation(), player.getPortalCooldown()), (byte)3));
                player.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
                PlayerList playerList = player.server.getPlayerList();
                playerList.sendPlayerPermissionLevel(player);
                ServerLevel oldLevel = player.serverLevel();
                oldLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
                player.unsetRemoved();
                player.setServerLevel(targetLevel);
                player.connection.teleport(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
                player.connection.resetPosition();
                targetLevel.addDuringPortalTeleport(player);
                player.triggerDimensionChangeTriggers(oldLevel);
                player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                playerList.sendLevelInfo(player, targetLevel);
                playerList.sendAllPlayerInfo(player);
                for(MobEffectInstance effectInstance : player.getActiveEffects())
                    player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effectInstance));
                player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
                player.lastSentExp = -1;
                player.lastSentHealth = -1.0f;
                player.lastSentFood = -1;
            }else{
                Entity newEntity = entity.getType().create(targetLevel);
                if(newEntity != null){
                    newEntity.restoreFrom(entity);
                    newEntity.moveTo(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
                    newEntity.setYHeadRot(target.yaw);
                    newEntity.setDeltaMovement(Vec3.ZERO);
                    newEntity.fallDistance = 0;
                    newEntity.setOnGround(true);
                    entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                    targetLevel.addDuringTeleport(newEntity);
                    return newEntity;
                }
            }
        }
        return entity;
    }
}
