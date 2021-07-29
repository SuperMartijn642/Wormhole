package com.supermartijn642.wormhole.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class WormholePacket {

    public WormholePacket(){
    }

    public WormholePacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
    }

    protected void decode(FriendlyByteBuf buffer){
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        Player player = contextSupplier.get().getSender();
        if(player == null)
            return;
        Level world = player.level;
        if(world == null)
            return;
        contextSupplier.get().enqueueWork(() -> this.handle(player, world));
    }

    protected abstract void handle(Player player, Level world);

}
