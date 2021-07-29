package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

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

    public PortalColorTargetPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.targetIndex);
        buffer.writeInt(this.color == null ? -1 : this.color.getId());
    }

    @Override
    protected void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.targetIndex = buffer.readInt();
        int color = buffer.readInt();
        this.color = color == -1 ? null : DyeColor.byId(color);
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        PortalTarget target = group.getTarget(this.targetIndex);
        if(target == null)
            return;
        target.color = this.color;
        group.setTarget(this.targetIndex, target);
    }
}
