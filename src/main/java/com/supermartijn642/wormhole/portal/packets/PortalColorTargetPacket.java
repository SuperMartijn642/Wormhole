package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalColorTargetPacket extends PortalGroupPacket<PortalColorTargetPacket> {

    private int targetIndex;
    private EnumDyeColor color;

    public PortalColorTargetPacket(PortalGroup group, int targetIndex, EnumDyeColor color){
        super(group);
        this.targetIndex = targetIndex;
        this.color = color;
    }

    public PortalColorTargetPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.targetIndex);
        buffer.writeInt(this.color == null ? -1 : this.color.getDyeDamage());
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.targetIndex = buffer.readInt();
        int color = buffer.readInt();
        this.color = color == -1 ? null : EnumDyeColor.byDyeDamage(color);
    }

    @Override
    protected void handle(PortalColorTargetPacket message, EntityPlayer player, World world, PortalGroup group){
        PortalTarget target = group.getTarget(message.targetIndex);
        if(target == null)
            return;
        target.color = message.color;
        group.setTarget(message.targetIndex, target);
    }
}
