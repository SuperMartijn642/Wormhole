package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalMoveTargetPacket extends PortalGroupPacket {

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
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        group.moveTarget(this.index, this.up);
    }
}
