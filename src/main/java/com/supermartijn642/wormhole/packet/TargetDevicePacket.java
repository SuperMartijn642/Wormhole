package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class TargetDevicePacket {

    private InteractionHand hand;

    public TargetDevicePacket(InteractionHand hand){
        this.hand = hand;
    }

    public TargetDevicePacket(FriendlyByteBuf buffer){
        this.decodeBuffer(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeEnum(this.hand);
    }

    protected void decodeBuffer(FriendlyByteBuf buffer){
        this.hand = buffer.readEnum(InteractionHand.class);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        Player player = contextSupplier.get().getSender();
        if(player == null)
            return;
        Level world = player.level;
        if(world == null)
            return;
        ItemStack stack = player.getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        contextSupplier.get().enqueueWork(() -> this.handle(player, world, stack));
    }

    protected abstract void handle(Player player, Level world, ItemStack targetDevice);

}
