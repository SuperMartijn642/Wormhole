package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalNameTargetPacket extends PortalGroupPacket {

    private int index;
    private String name;

    public PortalNameTargetPacket(PortalGroup group, int index, String name){
        super(group);
        this.index = index;
        this.name = name;
    }

    public PortalNameTargetPacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeString(this.name);
    }

    @Override
    protected void decode(PacketBuffer buffer){
        super.decode(buffer);
        this.index = buffer.readInt();
        this.name = buffer.readString(32767).trim();
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        PortalTarget target = group.getTarget(this.index);
        if(target != null){
            target.name = this.name;
            group.setTarget(this.index, target);
        }
    }
}
