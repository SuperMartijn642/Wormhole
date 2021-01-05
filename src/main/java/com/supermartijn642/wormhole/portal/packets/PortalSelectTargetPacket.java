package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalSelectTargetPacket extends PortalGroupPacket {

    private int target;

    public PortalSelectTargetPacket(PortalGroup group, int target){
        super(group);
        this.target = target;
    }

    public PortalSelectTargetPacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.target);
    }

    @Override
    protected void decode(PacketBuffer buffer){
        super.decode(buffer);
        this.target = buffer.readInt();
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        group.setActiveTarget(this.target);
    }
}
