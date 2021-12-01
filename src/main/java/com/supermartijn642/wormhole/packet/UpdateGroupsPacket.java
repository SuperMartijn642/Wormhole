package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupsPacket {

    private CompoundTag groupsData;

    public UpdateGroupsPacket(CompoundTag groupsData){
        this.groupsData = groupsData;
    }

    public UpdateGroupsPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeNbt(this.groupsData);
    }

    protected void decode(FriendlyByteBuf buffer){
        this.groupsData = buffer.readNbt();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);
        ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.read(this.groupsData));
    }
}
