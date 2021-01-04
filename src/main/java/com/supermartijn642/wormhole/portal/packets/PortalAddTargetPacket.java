package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalAddTargetPacket extends PortalGroupPacket {

    private Hand hand;
    private int index;

    public PortalAddTargetPacket(PortalGroup group, Hand hand, int index){
        super(group);
        this.hand = hand;
        this.index = index;
    }

    public PortalAddTargetPacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeEnumValue(this.hand);
        buffer.writeInt(this.index);
    }

    @Override
    protected void decode(PacketBuffer buffer){
        super.decode(buffer);
        this.hand = buffer.readEnumValue(Hand.class);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(PlayerEntity player, World world, PortalGroup group){
        ItemStack stack = player.getHeldItem(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        List<PortalTarget> targets = TargetDeviceItem.getTargets(stack);
        if(this.index >= 0 && this.index < targets.size())
            group.addTarget(targets.get(this.index));
    }
}
