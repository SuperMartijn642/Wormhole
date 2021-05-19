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
                ((ServerPlayerEntity)entity).connection.setPlayerLocation(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.setPositionAndUpdate(target.x + .5, target.y + .2, target.z + .5);
            entity.setRotationYawHead(target.yaw);
            entity.setMotion(Vec3d.ZERO);
            entity.fallDistance = 0;
            entity.onGround = true;
        }else if(net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity, targetWorld.dimension.getType()) && !entity.removed){
            if(entity instanceof ServerPlayerEntity){
                ServerPlayerEntity player = (ServerPlayerEntity)entity;
                DimensionType dimensiontype = entity.dimension;

                ServerWorld oldWorld = player.server.func_71218_a(dimensiontype);
                entity.dimension = targetWorld.dimension.getType();
                WorldInfo worldinfo = entity.world.getWorldInfo();
                net.minecraftforge.fml.network.NetworkHooks.sendDimensionDataPacket(player.connection.netManager, player);
                player.connection.sendPacket(new SRespawnPacket(targetWorld.dimension.getType(), worldinfo.getGenerator(), player.interactionManager.getGameType()));
                player.connection.sendPacket(new SServerDifficultyPacket(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
                PlayerList playerlist = player.server.getPlayerList();
                playerlist.updatePermissionLevel(player);
                oldWorld.removeEntity(player, true); //Forge: the player entity is moved to the new world, NOT cloned. So keep the data alive with no matching invalidate call.
                player.revive();

                player.setLocationAndAngles(target.x, target.y, target.z, target.yaw, 0);
                player.setMotion(Vec3d.ZERO);

                player.setWorld(targetWorld);
                targetWorld.func_217447_b(player);
                CriteriaTriggers.CHANGED_DIMENSION.trigger(player, dimensiontype, targetWorld.dimension.getType());
                player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                player.interactionManager.func_73080_a(targetWorld);
                player.connection.sendPacket(new SPlayerAbilitiesPacket(player.abilities));
                playerlist.func_72354_b(player, targetWorld);
                playerlist.sendInventory(player);

                for(EffectInstance effectinstance : player.getActivePotionEffects()){
                    player.connection.sendPacket(new SPlayEntityEffectPacket(player.getEntityId(), effectinstance));
                }

                player.connection.sendPacket(new SPlaySoundEventPacket(1032, BlockPos.ZERO, 0, false));
                player.setExperienceLevel(player.experienceLevel);
                net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerChangedDimensionEvent(player, dimensiontype, targetWorld.dimension.getType());
            }else{
                MinecraftServer minecraftserver = entity.getServer();
                ServerWorld oldWorld = minecraftserver.func_71218_a(entity.dimension);
                entity.dimension = targetWorld.dimension.getType();
                entity.detach();

                Entity newEntity = entity.getType().create(targetWorld);
                if(newEntity != null){
                    newEntity.copyDataFromOld(entity);
                    newEntity.moveToBlockPosAndAngles(target.getPos(), target.yaw, 0);
                    newEntity.setMotion(Vec3d.ZERO);
                    targetWorld.func_217460_e(newEntity);
                }

                entity.remove(false);
                oldWorld.resetUpdateEntityTick();
                targetWorld.resetUpdateEntityTick();
                return newEntity;
            }
        }
        return entity;
    }
}
