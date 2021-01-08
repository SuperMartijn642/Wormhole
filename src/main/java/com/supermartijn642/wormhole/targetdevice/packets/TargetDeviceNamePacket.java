package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceNamePacket extends TargetDevicePacket<TargetDeviceNamePacket> {

    private int index;
    private String name;

    public TargetDeviceNamePacket(EnumHand hand, int index, String name){
        super(hand);
        this.index = index;
        this.name = name;
    }

    public TargetDeviceNamePacket(){
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
    protected void handle(TargetDeviceNamePacket message, EntityPlayer player, World world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(message.index < 0 || message.index > targets.size() - 1)
            return;
        TargetDeviceItem.changeTargetName(targetDevice, message.index, ChatAllowedCharacters.filterAllowedCharacters(message.name));
    }
}
