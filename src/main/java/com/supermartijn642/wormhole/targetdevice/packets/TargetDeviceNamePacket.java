package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceNamePacket extends TargetDevicePacket {

    private int index;
    private String name;

    public TargetDeviceNamePacket(InteractionHand hand, int index, String name){
        super(hand);
        this.index = index;
        this.name = name;
    }

    public TargetDeviceNamePacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeUtf(this.name);
    }

    @Override
    protected void decodeBuffer(FriendlyByteBuf buffer){
        super.decodeBuffer(buffer);
        this.index = buffer.readInt();
        this.name = buffer.readUtf(32767).trim();
    }

    @Override
    protected void handle(Player player, Level world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(this.index < 0 || this.index > targets.size() - 1)
            return;
        TargetDeviceItem.changeTargetName(targetDevice, this.index, this.name);
    }
}
