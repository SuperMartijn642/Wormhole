package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

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

    public PortalMoveTargetPacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    protected void decode(PacketBuffer buffer){
        super.decode(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        group.moveTarget(this.index, this.up);
    }
}
