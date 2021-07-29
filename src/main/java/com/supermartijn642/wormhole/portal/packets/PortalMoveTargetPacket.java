package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

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

    public PortalMoveTargetPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    protected void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        group.moveTarget(this.index, this.up);
    }
}
