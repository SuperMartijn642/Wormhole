package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class TargetDevicePacket implements BasePacket {

    private Hand hand;

    public TargetDevicePacket(Hand hand){
        this.hand = hand;
    }

    public TargetDevicePacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeBoolean(this.hand == Hand.MAIN_HAND);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    @Override
    public void handle(PacketContext context){
        PlayerEntity player = context.getSendingPlayer();
        if(player == null)
            return;
        World level = context.getWorld();
        if(level == null)
            return;
        ItemStack stack = player.getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        this.handle(stack, context);
    }

    protected abstract void handle(ItemStack targetDevice, PacketContext context);

}
