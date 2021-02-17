package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import java.util.Optional;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class TeleportHelper {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public static void queTeleport(Entity entity, PortalTarget target){
        if(entity.world.isRemote)
            return;

        entity.world.getMinecraftServer().addScheduledTask(() -> teleport(entity, target));
    }

    public static void teleport(Entity entity, PortalTarget target){
        if(!(entity.world instanceof WorldServer) || !target.getWorld(entity.getServer()).isPresent())
            return;

        NBTTagCompound tag = entity.getEntityData();
        if(tag.hasKey("wormhole:teleported") && entity.ticksExisted - tag.getLong("wormhole:teleported") > 0 && entity.ticksExisted - tag.getLong("wormhole:teleported") < TELEPORT_COOLDOWN)
            return;

        Optional<WorldServer> optionalTargetWorld = target.getWorld(entity.getServer()).filter(WorldServer.class::isInstance).map(WorldServer.class::cast);
        if(!optionalTargetWorld.isPresent())
            return;

        WorldServer targetWorld = optionalTargetWorld.get();

        if(targetWorld == entity.world){
            if(entity instanceof EntityPlayerMP)
                ((EntityPlayerMP)entity).connection.setPlayerLocation(target.x + .5, target.y, target.z + .5, target.yaw, 0);
            else
                entity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
            entity.setRotationYawHead(target.yaw);
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
            entity.fallDistance = 0;
            entity.onGround = true;
        }else{
            entity = entity.changeDimension(targetWorld.provider.getDimensionType().getId(), (world, entity1, yaw) -> entity1.moveToBlockPosAndAngles(target.getPos(), target.yaw, entity1.rotationPitch));
            if(entity != null){
                entity.motionX = 0;
                entity.motionY = 0;
                entity.motionZ = 0;
                entity.fallDistance = 0;
                entity.onGround = true;
            }
        }

        tag.setLong("wormhole:teleported", entity.ticksExisted);
    }

    /**
     * Copy the logic from {@link Entity#copyDataFromOld(Entity)}, since that is private in 1.12
     */
    private static void copyDataFromOld(Entity from, Entity to){
        NBTTagCompound nbttagcompound = from.writeToNBT(new NBTTagCompound());
        nbttagcompound.removeTag("Dimension");
        to.readFromNBT(nbttagcompound);
        to.timeUntilPortal = from.timeUntilPortal;
        // copying the other 3 portal related variables shouldn't be necessary
    }
}
