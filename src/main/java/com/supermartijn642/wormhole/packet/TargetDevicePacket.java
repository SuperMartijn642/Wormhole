package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class TargetDevicePacket {

    private Hand hand;

    public TargetDevicePacket(Hand hand){
        this.hand = hand;
    }

    public TargetDevicePacket(PacketBuffer buffer){
        this.decodeBuffer(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeEnum(this.hand);
    }

    protected void decodeBuffer(PacketBuffer buffer){
        this.hand = buffer.readEnum(Hand.class);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        PlayerEntity player = contextSupplier.get().getSender();
        if(player == null)
            return;
        World world = player.level;
        if(world == null)
            return;
        ItemStack stack = player.getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        contextSupplier.get().enqueueWork(() -> this.handle(player, world, stack));
    }

    protected abstract void handle(PlayerEntity player, World world, ItemStack targetDevice);

}
