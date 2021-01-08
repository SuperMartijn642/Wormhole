package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalGroupCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class UpdateGroupsPacket extends WormholePacket<UpdateGroupsPacket> {

    private NBTTagCompound groupsData;

    public UpdateGroupsPacket(NBTTagCompound groupsData){
        this.groupsData = groupsData;
    }

    public UpdateGroupsPacket(){
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, this.groupsData);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.groupsData = ByteBufUtils.readTag(buf);
    }

    @Override
    protected void handle(UpdateGroupsPacket message, EntityPlayer player, World world){
        PortalGroupCapability groups = ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY, null);
        if(groups != null)
            groups.read(message.groupsData);
    }
}
