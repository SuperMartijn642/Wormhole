package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalActivatePacket extends PortalGroupPacket {
    public PortalActivatePacket(PortalGroup group){
        super(group);
    }

    public PortalActivatePacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        group.activate();
    }
}
