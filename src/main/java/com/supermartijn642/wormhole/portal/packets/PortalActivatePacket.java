package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalActivatePacket extends PortalGroupPacket<PortalActivatePacket> {
    public PortalActivatePacket(PortalGroup group){
        super(group);
    }

    public PortalActivatePacket(){
    }

    @Override
    protected void handle(PortalActivatePacket message, EntityPlayer player, World world, PortalGroup group){
        group.activate();
    }
}
