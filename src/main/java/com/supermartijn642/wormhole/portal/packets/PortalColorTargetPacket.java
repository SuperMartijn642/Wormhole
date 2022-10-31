package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.PacketBuffer;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalColorTargetPacket extends PortalGroupPacket {

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
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.targetIndex);
        buffer.writeInt(this.color == null ? -1 : this.color.getDyeDamage());
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.targetIndex = buffer.readInt();
        int color = buffer.readInt();
        this.color = color == -1 ? null : EnumDyeColor.byDyeDamage(color);
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        PortalTarget target = group.getTarget(this.targetIndex);
        if(target == null)
            return;
        target.color = this.color;
        group.setTarget(this.targetIndex, target);
    }
}
