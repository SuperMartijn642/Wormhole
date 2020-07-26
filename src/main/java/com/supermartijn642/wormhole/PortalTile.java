package com.supermartijn642.wormhole;

import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    public PortalTile(){
        super(Wormhole.portal_tile);
    }

    public void teleport(Entity entity){
        if(this.group != null && this.group.getTarget() != null){
            PortalTarget target = this.group.getTarget();
            target.getWorld(this.world.getServer()).filter(world -> world instanceof ServerWorld).map(ServerWorld.class::cast).ifPresent(world -> {
                if(entity instanceof ServerPlayerEntity){
                    ServerPlayerEntity player = (ServerPlayerEntity)entity;
//                    ChunkPos chunkpos = new ChunkPos(target.getPos());
//                    world.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, entity.getEntityId());
                    entity.stopRiding();

                    if(player.isSleeping())
                        player.stopSleepInBed(true, true);

                    if(world == entity.world)
                        player.connection.setPlayerLocation(target.x + .5, target.y, target.z + .5, target.yaw, 0, Collections.emptySet());
                    else
                        player.teleport(world, target.x + .5, target.y, target.z + .5, target.yaw, 0);

                    entity.setRotationYawHead(target.yaw);
                }else{
                    if(world == entity.world){
                        entity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                        entity.setRotationYawHead(target.yaw);
                    }else{
                        entity.detach();
                        Entity newEntity = entity.getType().create(world);
                        if(newEntity == null)
                            return;

                        newEntity.copyDataFromOld(entity);
                        newEntity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                        newEntity.setRotationYawHead(target.yaw);
                        world.addFromAnotherDimension(newEntity);
                    }
                }

                if(!(entity instanceof LivingEntity) || !((LivingEntity)entity).isElytraFlying()){
                    entity.setMotion(Vector3d.ZERO);
                    entity.func_230245_c_(true);
                }

                if(entity instanceof CreatureEntity)
                    ((CreatureEntity)entity).getNavigator().clearPath();
            });
        }
    }

    public boolean activate(PlayerEntity player, Hand hand){
        if(player.getHeldItem(hand).getItem() instanceof DyeItem){
            DyeColor color = ((DyeItem)player.getHeldItem(hand).getItem()).getDyeColor();
            if(this.group != null && this.group.getTarget() != null){
                for(BlockPos pos : this.group.shape.area){
                    BlockState state = this.world.getBlockState(pos);
                    if(state.getBlock() == Wormhole.portal && state.get(PortalBlock.COLOR_PROPERTY) != color)
                        this.world.setBlockState(pos, state.with(PortalBlock.COLOR_PROPERTY, color));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBreak(){
        if(this.group != null)
            this.group.removeTarget();
    }
}
