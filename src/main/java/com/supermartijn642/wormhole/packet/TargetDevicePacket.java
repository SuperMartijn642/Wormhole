package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class TargetDevicePacket implements BasePacket {

    private InteractionHand hand;

    public TargetDevicePacket(InteractionHand hand){
        this.hand = hand;
    }

    public TargetDevicePacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBoolean(this.hand == InteractionHand.MAIN_HAND);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getSendingPlayer();
        if(player == null)
            return;
        Level level = context.getWorld();
        if(level == null)
            return;
        ItemStack stack = player.getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        this.handle(stack, context);
    }

    protected abstract void handle(ItemStack targetDevice, PacketContext context);

}
