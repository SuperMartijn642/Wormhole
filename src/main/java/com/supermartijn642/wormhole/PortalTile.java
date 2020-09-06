package com.supermartijn642.wormhole;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    public final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public PortalTile(){
        super();
    }

    public void teleport(Entity entity){
        if(this.group != null && this.group.getTarget() != null){
            PortalTarget target = this.group.getTarget();

            if(!this.world.isRemote)
                this.world.getMinecraftServer().addScheduledTask(() -> {
                    target.getWorld(this.world.getMinecraftServer()).filter(world -> world instanceof WorldServer).map(WorldServer.class::cast).ifPresent(world -> {
                        NBTTagCompound tag = entity.getEntityData();
                        if(!tag.hasKey("wormhole:teleported") || entity.ticksExisted - tag.getLong("wormhole:teleported") < 0 || entity.ticksExisted - tag.getLong("wormhole:teleported") > TELEPORT_COOLDOWN){
                            entity.dismountRidingEntity();

                            if(entity.getEntityWorld() != world)
                                entity.changeDimension(world.provider.getDimensionType().getId(), (world1, entity1, yaw) -> entity1.moveToBlockPosAndAngles(target.getPos(), yaw, entity1.rotationPitch));

                            if(entity instanceof EntityPlayerMP)
                                ((EntityPlayerMP)entity).connection.setPlayerLocation(target.x + .5, target.y, target.z + .5, target.yaw, 0, Collections.emptySet());
                            else
                                entity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                            entity.setRotationYawHead(target.yaw);

                            if(!(entity instanceof EntityLivingBase) || !((EntityLivingBase)entity).isElytraFlying()){
                                entity.motionY = 0.0D;
                                entity.onGround = true;
                            }

                            tag.setLong("wormhole:teleported", entity.ticksExisted);
                        }
                    });
                });
        }
    }

    public boolean activate(EntityPlayer player, EnumHand hand){
        if(player.getHeldItem(hand).getItem() instanceof ItemDye){
            EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(hand).getMetadata());
            if(this.group != null && this.group.getTarget() != null){
                for(BlockPos pos : this.group.shape.area){
                    IBlockState state = this.world.getBlockState(pos);
                    if(state.getBlock() == this.getBlockType() && state.getValue(PortalBlock.COLOR_PROPERTY) != color){
                        this.world.setBlockState(pos, state.withProperty(PortalBlock.COLOR_PROPERTY, color));
                        TileEntity tile = this.world.getTileEntity(pos);
                        if(tile instanceof PortalTile)
                            ((PortalTile)tile).setGroup(this.group);
                    }
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
