package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetPassengersPacket;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.Collection;
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
        if(entity.level.isClientSide || !target.getWorld(entity.getServer()).isPresent())
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
        if(entity.level.isClientSide || !target.getWorld(entity.getServer()).isPresent())
            return;
        Optional<ServerWorld> targetWorld = target.getWorld(entity.getServer()).filter(ServerWorld.class::isInstance).map(ServerWorld.class::cast);
        if(!targetWorld.isPresent())
            return;

        Collection<Entity> passengers = entity.getPassengers();
        entity.ejectPassengers();
        Entity newEntity = teleportEntity(entity, targetWorld.get(), target);
        if(entityBeingRidden != null){
            newEntity.startRiding(entityBeingRidden);
            if(newEntity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)newEntity).connection.send(new SSetPassengersPacket(entityBeingRidden));
        }
        passengers.forEach(e -> teleportEntityAndPassengers(e, newEntity, target));
    }

    private static Entity teleportEntity(Entity entity, ServerWorld targetWorld, PortalTarget target){
        if(targetWorld == entity.level){
            if(entity instanceof ServerPlayerEntity)
                ((ServerPlayerEntity)entity).teleportTo(targetWorld, target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.teleportTo(target.x + .5, target.y + .2, target.z + .5);
            entity.setYHeadRot(target.yaw);
            entity.setDeltaMovement(Vector3d.ZERO);
            entity.fallDistance = 0;
            entity.setOnGround(true);
            return entity;
        }else
            return entity.changeDimension(targetWorld, new WormholeTeleporter(target));
    }

    private static class WormholeTeleporter implements ITeleporter {
        private final PortalTarget target;

        public WormholeTeleporter(PortalTarget target){
            this.target = target;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean,Entity> repositionEntity){
            return repositionEntity.apply(false);
        }

        @Nullable
        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld,PortalInfo> defaultPortalInfo){
            return new PortalInfo(this.target.getCenteredPos(), Vector3d.ZERO, this.target.yaw, 0);
        }
    }
}
