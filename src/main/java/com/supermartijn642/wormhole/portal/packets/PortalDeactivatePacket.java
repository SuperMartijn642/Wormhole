package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class PortalDeactivatePacket extends PortalGroupPacket {
    public PortalDeactivatePacket(PortalGroup group){
        super(group);
    }

    public PortalDeactivatePacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        group.deactivate();
    }
}
