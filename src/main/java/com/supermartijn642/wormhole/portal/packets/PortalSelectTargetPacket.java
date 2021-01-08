package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalSelectTargetPacket extends PortalGroupPacket<PortalSelectTargetPacket> {

    private int target;

    public PortalSelectTargetPacket(PortalGroup group, int target){
        super(group);
        this.target = target;
    }

    public PortalSelectTargetPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.target);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.target = buffer.readInt();
    }

    @Override
    protected void handle(PortalSelectTargetPacket message, EntityPlayer player, World world, PortalGroup group){
        group.setActiveTarget(message.target);
    }
}
