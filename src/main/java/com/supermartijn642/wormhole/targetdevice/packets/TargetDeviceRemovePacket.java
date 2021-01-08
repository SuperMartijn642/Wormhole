package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceRemovePacket extends TargetDevicePacket {

    private int index;

    public TargetDeviceRemovePacket(Hand hand, int index){
        super(hand);
        this.index = index;
    }

    public TargetDeviceRemovePacket(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
    }

    @Override
    protected void decodeBuffer(PacketBuffer buffer){
        super.decodeBuffer(buffer);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(PlayerEntity player, World world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(this.index < 0 || this.index > targets.size() - 1)
            return;
        TargetDeviceItem.removeTarget(targetDevice, this.index);
    }
}
