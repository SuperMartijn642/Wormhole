package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceRemovePacket extends TargetDevicePacket {

    private int index;

    public TargetDeviceRemovePacket(InteractionHand hand, int index){
        super(hand);
        this.index = index;
    }

    public TargetDeviceRemovePacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeInt(this.index);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        this.index = buffer.readInt();
    }

    @Override
    protected void handle(ItemStack targetDevice, PacketContext context){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(this.index < 0 || this.index > targets.size() - 1)
            return;
        TargetDeviceItem.removeTarget(targetDevice, this.index);
    }
}
