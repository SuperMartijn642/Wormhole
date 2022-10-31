package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import java.util.List;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalAddTargetPacket extends PortalGroupPacket {

    private EnumHand hand;
    private int index;

    public PortalAddTargetPacket(PortalGroup group, EnumHand hand, int index){
        super(group);
        this.hand = hand;
        this.index = index;
    }

    public PortalAddTargetPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeEnumValue(this.hand);
        buffer.writeInt(this.index);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.hand = buffer.readEnumValue(EnumHand.class);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(PortalGroup group, PacketContext context){
        ItemStack stack = context.getSendingPlayer().getHeldItem(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        List<PortalTarget> targets = TargetDeviceItem.getTargets(stack);
        if(this.index >= 0 && this.index < targets.size())
            group.addTarget(targets.get(this.index));
    }
}
