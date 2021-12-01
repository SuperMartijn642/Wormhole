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
public class UpdateGroupPacket {

    private CompoundTag groupData;

    public UpdateGroupPacket(CompoundTag groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeNbt(this.groupData);
    }

    protected void decode(FriendlyByteBuf buffer){
        this.groupData = buffer.readNbt();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);
        ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.readGroup(this.groupData));
    }
}
