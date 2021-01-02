package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupPacket {

    private CompoundNBT groupData;

    public UpdateGroupPacket(CompoundNBT groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeCompoundTag(this.groupData);
    }

    protected void decode(PacketBuffer buffer){
        this.groupData = buffer.readCompoundTag();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);
        ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.readGroup(this.groupData));
    }
}
