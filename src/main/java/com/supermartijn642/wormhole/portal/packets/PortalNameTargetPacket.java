package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.network.PacketBuffer;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalNameTargetPacket extends PortalGroupPacket {

    private int index;
    private String name;

    public PortalNameTargetPacket(PortalGroup group, int index, String name){
        super(group);
        this.index = index;
        this.name = name;
    }

    public PortalNameTargetPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.index);
        buffer.writeUtf(this.name);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.index = buffer.readInt();
        this.name = buffer.readUtf(32767).trim();
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        PortalTarget target = group.getTarget(this.index);
        if(target != null){
            target.name = this.name;
            group.setTarget(this.index, target);
        }
    }
}
