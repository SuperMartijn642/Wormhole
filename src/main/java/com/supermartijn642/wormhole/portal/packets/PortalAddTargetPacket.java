package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalAddTargetPacket extends PortalGroupPacket<PortalAddTargetPacket> {

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
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeBoolean(this.hand == EnumHand.MAIN_HAND);
        buffer.writeInt(this.index);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.hand = buffer.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(PortalAddTargetPacket message, EntityPlayer player, World world, PortalGroup group){
        ItemStack stack = player.getHeldItem(message.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        List<PortalTarget> targets = TargetDeviceItem.getTargets(stack);
        if(message.index >= 0 && message.index < targets.size())
            group.addTarget(targets.get(message.index));
    }
}
