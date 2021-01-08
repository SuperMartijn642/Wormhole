package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class TargetDevicePacket<T extends TargetDevicePacket> extends WormholePacket<T> {

    protected EnumHand hand;

    public TargetDevicePacket(EnumHand hand){
        this.hand = hand;
    }

    public TargetDevicePacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeBoolean(this.hand == EnumHand.MAIN_HAND);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.hand = buffer.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    @Override
    protected void handle(T message, EntityPlayer player, World world){
        ItemStack stack = player.getHeldItem(message.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        this.handle(message, player, world, stack);
    }

    protected abstract void handle(T message, EntityPlayer player, World world, ItemStack targetDevice);

}
