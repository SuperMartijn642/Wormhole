package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalMoveTargetPacket extends PortalGroupPacket<PortalMoveTargetPacket> {

    private int index;
    private boolean up;

    public PortalMoveTargetPacket(PortalGroup group, int index, boolean up){
        super(group);
        this.index = index;
        this.up = up;
    }

    public PortalMoveTargetPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(PortalMoveTargetPacket message, EntityPlayer player, World world, PortalGroup group){
        group.moveTarget(message.index, message.up);
    }
}
