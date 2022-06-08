package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalAddTargetPacket extends PortalGroupPacket {

    private InteractionHand hand;
    private int index;

    public PortalAddTargetPacket(PortalGroup group, InteractionHand hand, int index){
        super(group);
        this.hand = hand;
        this.index = index;
    }

    public PortalAddTargetPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeEnum(this.hand);
        buffer.writeInt(this.index);
    }

    @Override
    protected void decode(FriendlyByteBuf buffer){
        super.decode(buffer);
        this.hand = buffer.readEnum(InteractionHand.class);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(Player player, Level world, PortalGroup group){
        ItemStack stack = player.getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        List<PortalTarget> targets = TargetDeviceItem.getTargets(stack);
        if(this.index >= 0 && this.index < targets.size())
            group.addTarget(targets.get(this.index));
    }
}
