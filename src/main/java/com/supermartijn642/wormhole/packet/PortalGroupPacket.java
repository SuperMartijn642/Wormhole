package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.portal.PortalGroup;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class PortalGroupPacket<T extends PortalGroupPacket> extends WormholePacket<T> {

    protected BlockPos pos;

    public PortalGroupPacket(PortalGroup group){
        this.pos = group.shape.frame.get(0);
    }

    public PortalGroupPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.pos.getX());
        buffer.writeInt(this.pos.getY());
        buffer.writeInt(this.pos.getZ());
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    protected void handle(T message, EntityPlayer player, World world){
        PortalGroupCapability groups = world.getCapability(PortalGroupCapability.CAPABILITY, null);
        if(groups == null)
            return;
        PortalGroup group = groups.getGroup(message.pos);
        if(group == null)
            return;
        this.handle(message, player, world, group);
    }

    protected abstract void handle(T message, EntityPlayer player, World world, PortalGroup group);

}
