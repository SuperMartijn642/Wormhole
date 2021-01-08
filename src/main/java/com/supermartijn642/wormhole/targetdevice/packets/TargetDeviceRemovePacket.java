package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceRemovePacket extends TargetDevicePacket<TargetDeviceRemovePacket> {

    private int index;

    public TargetDeviceRemovePacket(EnumHand hand, int index){
        super(hand);
        this.index = index;
    }

    public TargetDeviceRemovePacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.index);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(TargetDeviceRemovePacket message, EntityPlayer player, World world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(message.index < 0 || message.index > targets.size() - 1)
            return;
        TargetDeviceItem.removeTarget(targetDevice, message.index);
    }
}
