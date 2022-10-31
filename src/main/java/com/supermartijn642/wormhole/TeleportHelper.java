package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class TeleportHelper {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    private static final List<Runnable> tpQueue = new LinkedList<>();

    public static boolean queTeleport(Entity entity, PortalTarget target){
        if(!canTeleport(entity, target))
            return false;
        for(Entity passenger : entity.getRecursivePassengers())
            if(passenger instanceof EntityPlayer)
                return false;

        Entity lowestEntity = entity.getLowestRidingEntity();
        if(!entity.world.isRemote){
            tpQueue.add(() -> teleportEntityAndPassengers(lowestEntity, null, target));
            markEntityAndPassengers(lowestEntity);
        }
        return true;
    }

    public static boolean canTeleport(Entity entity, PortalTarget target){
        if(entity.world.isRemote || !target.getLevel(entity.getServer()).isPresent())
            return false;
        if(entity.isPassenger(entity))
            return canTeleport(entity.getLowestRidingEntity(), target);

        for(Entity rider : entity.getRecursivePassengers()){
            NBTTagCompound tag = rider.getEntityData();
            if(tag.hasKey("wormhole:teleported") && rider.ticksExisted - tag.getLong("wormhole:teleported") >= 0 && rider.ticksExisted - tag.getLong("wormhole:teleported") < TELEPORT_COOLDOWN)
                return false;
        }

        NBTTagCompound tag = entity.getEntityData();
        return !tag.hasKey("wormhole:teleported") || entity.ticksExisted - tag.getLong("wormhole:teleported") < 0 || entity.ticksExisted - tag.getLong("wormhole:teleported") >= TELEPORT_COOLDOWN;
    }

    private static void markEntityAndPassengers(Entity entity){
        entity.getEntityData().setLong("wormhole:teleported", entity.ticksExisted);
        entity.getPassengers().forEach(TeleportHelper::markEntityAndPassengers);
    }

    private static void teleportEntityAndPassengers(Entity entity, Entity entityBeingRidden, PortalTarget target){
        if(entity.world.isRemote || !target.getLevel(entity.getServer()).isPresent())
            return;
        Optional<WorldServer> targetLevel = target.getLevel(entity.getServer()).filter(WorldServer.class::isInstance).map(WorldServer.class::cast);
        if(!targetLevel.isPresent())
            return;

        Collection<Entity> passengers = entity.getPassengers();
        entity.removePassengers();
        Entity newEntity = teleportEntity(entity, targetLevel.get(), target);
        if(entityBeingRidden != null){
            newEntity.startRiding(entityBeingRidden);
            if(newEntity instanceof EntityPlayerMP)
                ((EntityPlayerMP)newEntity).connection.sendPacket(new SPacketSetPassengers(entityBeingRidden));
        }
        passengers.forEach(e -> teleportEntityAndPassengers(e, newEntity, target));
    }

    private static Entity teleportEntity(Entity entity, WorldServer targetLevel, PortalTarget target){
        if(targetLevel == entity.world){
            if(entity instanceof EntityPlayerMP)
                ((EntityPlayerMP)entity).connection.setPlayerLocation(target.x + .5, target.y + .2, target.z + .5, target.yaw, 0);
            else
                entity.setPositionAndUpdate(target.x + .5, target.y + .2, target.z + .5);
            entity.setRotationYawHead(target.yaw);
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
            entity.fallDistance = 0;
            entity.onGround = true;
        }else{
            Entity newEntity = entity.changeDimension(targetLevel.provider.getDimensionType().getId(), (world, entity1, yaw) -> entity1.moveToBlockPosAndAngles(target.getPos(), target.yaw, entity1.rotationPitch));
            if(entity != null){
                entity.motionX = 0;
                entity.motionY = 0;
                entity.motionZ = 0;
                entity.fallDistance = 0;
                entity.onGround = true;
                return newEntity;
            }
        }
        return entity;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.ServerTickEvent e){
        if(e.phase == TickEvent.Phase.START){
            tpQueue.forEach(Runnable::run);
            tpQueue.clear();
        }
    }
}
