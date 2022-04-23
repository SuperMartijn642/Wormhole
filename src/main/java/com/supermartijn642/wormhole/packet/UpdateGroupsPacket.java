package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupsPacket {

    private CompoundNBT groupsData;

    public UpdateGroupsPacket(CompoundNBT groupsData){
        this.groupsData = groupsData;
    }

    public UpdateGroupsPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeNbt(this.groupsData);
    }

    protected void decode(PacketBuffer buffer){
        this.groupsData = buffer.readNbt();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);
        ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.read(this.groupsData));
    }
}
