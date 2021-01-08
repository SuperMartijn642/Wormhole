package com.supermartijn642.wormhole.portal.packets;

import com.supermartijn642.wormhole.packet.PortalGroupPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalNameTargetPacket extends PortalGroupPacket<PortalNameTargetPacket> {

    private int index;
    private String name;

    public PortalNameTargetPacket(PortalGroup group, int index, String name){
        super(group);
        this.index = index;
        this.name = name;
    }

    public PortalNameTargetPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.index);
        ByteBufUtils.writeUTF8String(buffer, this.name);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.index = buffer.readInt();
        this.name = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void handle(PortalNameTargetPacket message, EntityPlayer player, World world, PortalGroup group){
        PortalTarget target = group.getTarget(message.index);
        if(target != null){
            target.name = ChatAllowedCharacters.filterAllowedCharacters(message.name);
            group.setTarget(message.index, target);
        }
    }
}
