package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public PortalNameTargetPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeUtf(this.name);
    }

    @Override
    protected void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.index = buffer.readInt();
        this.name = buffer.readUtf(32767).trim();
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        PortalTarget target = group.getTarget(this.index);
        if(target != null){
            target.name = this.name;
            group.setTarget(this.index, target);
        }
    }
}
