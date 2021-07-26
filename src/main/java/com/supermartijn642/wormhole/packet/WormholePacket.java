package com.supermartijn642.wormhole.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class WormholePacket {

    public WormholePacket(){
    }

    public WormholePacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
    }

    protected void decode(PacketBuffer buffer){
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        PlayerEntity player = contextSupplier.get().getSender();
        if(player == null)
            return;
        World world = player.level;
        if(world == null)
            return;
        contextSupplier.get().enqueueWork(() -> this.handle(player, world));
    }

    protected abstract void handle(PlayerEntity player, World world);

}
