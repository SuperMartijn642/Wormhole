package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupPacket implements BasePacket {

    private CompoundTag groupData;

    public UpdateGroupPacket(CompoundTag groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeNbt(this.groupData);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.groupData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        context.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.readGroup(this.groupData));
    }
}
