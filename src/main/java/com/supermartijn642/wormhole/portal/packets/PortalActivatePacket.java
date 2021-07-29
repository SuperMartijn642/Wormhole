package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalActivatePacket extends PortalGroupPacket {
    public PortalActivatePacket(PortalGroup group){
        super(group);
    }

    public PortalActivatePacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        group.activate();
    }
}
