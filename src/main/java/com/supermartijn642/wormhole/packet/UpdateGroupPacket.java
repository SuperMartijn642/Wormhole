package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupPacket implements BasePacket {

    private CompoundNBT groupData;

    public UpdateGroupPacket(CompoundNBT groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeNbt(this.groupData);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.groupData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        context.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.readGroup(this.groupData));
    }
}
