package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalColorTargetPacket extends PortalGroupPacket {

    private int targetIndex;
    private DyeColor color;

    public PortalColorTargetPacket(PortalGroup group, int targetIndex, DyeColor color){
        super(group);
        this.targetIndex = targetIndex;
        this.color = color;
    }

    public PortalColorTargetPacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.targetIndex);
        buffer.writeInt(this.color == null ? -1 : this.color.getId());
    }

    @Override
    protected void decode(PacketBuffer buffer){
        super.decode(buffer);
        this.targetIndex = buffer.readInt();
        int color = buffer.readInt();
        this.color = color == -1 ? null : DyeColor.byId(color);
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        PortalTarget target = group.getTarget(this.targetIndex);
        if(target == null)
            return;
        target.color = this.color;
        group.setTarget(this.targetIndex, target);
    }
}
