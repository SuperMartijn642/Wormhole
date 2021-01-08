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
public class TargetDeviceMovePacket extends TargetDevicePacket<TargetDeviceMovePacket> {

    private int index;
    private boolean up;

    public TargetDeviceMovePacket(EnumHand hand, int index, boolean up){
        super(hand);
        this.index = index;
        this.up = up;
    }

    public TargetDeviceMovePacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(TargetDeviceMovePacket message, EntityPlayer player, World world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(message.index < 0 || message.index > targets.size() - 1)
            return;
        TargetDeviceItem.moveTarget(targetDevice, message.index, message.up);
    }
}
