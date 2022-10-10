package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalActivatePacket extends PortalGroupPacket {

    public PortalActivatePacket(PortalGroup group){
        super(group);
    }

    public PortalActivatePacket(){
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        group.activate();
    }
}
