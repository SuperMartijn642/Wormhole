package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceNamePacket extends TargetDevicePacket {

    private int index;
    private String name;

    public TargetDeviceNamePacket(Hand hand, int index, String name){
        super(hand);
        this.index = index;
        this.name = name;
    }

    public TargetDeviceNamePacket(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.index);
        buffer.writeUtf(this.name);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.index = buffer.readInt();
        this.name = buffer.readUtf(32767).trim();
    }

    @Override
    protected void handle(ItemStack targetDevice, PacketContext context){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(this.index < 0 || this.index > targets.size() - 1)
            return;
        TargetDeviceItem.changeTargetName(targetDevice, this.index, this.name);
    }
}
