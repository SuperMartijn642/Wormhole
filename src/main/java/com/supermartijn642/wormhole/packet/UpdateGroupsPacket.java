package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupsPacket implements BasePacket {

    private CompoundNBT groupsData;

    public UpdateGroupsPacket(CompoundNBT groupsData){
        this.groupsData = groupsData;
    }

    public UpdateGroupsPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeNbt(this.groupsData);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.groupsData = buffer.readNbt();
    }

    @Override
    public void handle(PacketContext context){
        context.getWorld().getCapability(PortalGroupCapability.CAPABILITY).ifPresent(groups -> groups.read(this.groupsData));
    }
}
