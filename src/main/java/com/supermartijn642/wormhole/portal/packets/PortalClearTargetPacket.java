package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.network.PacketBuffer;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalClearTargetPacket extends PortalGroupPacket {

    private int target;

    public PortalClearTargetPacket(PortalGroup group, int target){
        super(group);
        this.target = target;
    }

    public PortalClearTargetPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.target);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.target = buffer.readInt();
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        group.clearTarget(this.target);
    }
}
