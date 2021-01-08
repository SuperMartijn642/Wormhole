package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalDeactivatePacket extends PortalGroupPacket<PortalDeactivatePacket> {
    public PortalDeactivatePacket(PortalGroup group){
        super(group);
    }

    public PortalDeactivatePacket(){
    }

    @Override
    protected void handle(PortalDeactivatePacket message, EntityPlayer player, World world, PortalGroup group){
        group.deactivate();
    }
}
