package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalSelectTargetPacket extends PortalGroupPacket {

    private int target;

    public PortalSelectTargetPacket(PortalGroup group, int target){
        super(group);
        this.target = target;
    }

    public PortalSelectTargetPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.target);
    }

    @Override
    protected void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.target = buffer.readInt();
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        group.setActiveTarget(this.target);
    }
}
