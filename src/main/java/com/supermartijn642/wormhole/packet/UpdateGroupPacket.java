package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.PortalGroupCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupPacket implements BasePacket {

    private NBTTagCompound groupData;

    public UpdateGroupPacket(NBTTagCompound groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        ByteBufUtils.writeTag(buffer, this.groupData);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.groupData = ByteBufUtils.readTag(buffer);
    }

    @Override
    public void handle(PacketContext context){
        PortalGroupCapability groups = context.getWorld().getCapability(PortalGroupCapability.CAPABILITY, null);
        if(groups != null)
            groups.readGroup(this.groupData);
    }
}
