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
public class UpdateGroupPacket extends WormholePacket<UpdateGroupPacket> {

    private NBTTagCompound groupData;

    public UpdateGroupPacket(NBTTagCompound groupData){
        this.groupData = groupData;
    }

    public UpdateGroupPacket(){
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, this.groupData);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.groupData = ByteBufUtils.readTag(buf);
    }

    @Override
    protected void handle(UpdateGroupPacket message, EntityPlayer player, World world){
        PortalGroupCapability groups = ClientProxy.getWorld().getCapability(PortalGroupCapability.CAPABILITY, null);
        if(groups != null)
            groups.readGroup(message.groupData);
    }
}
