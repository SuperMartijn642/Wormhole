package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceMovePacket extends TargetDevicePacket {

    private int index;
    private boolean up;

    public TargetDeviceMovePacket(InteractionHand hand, int index, boolean up){
        super(hand);
        this.index = index;
        this.up = up;
    }

    public TargetDeviceMovePacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.up);
    }

    @Override
    protected void decodeBuffer(FriendlyByteBuf buffer){
        super.decodeBuffer(buffer);
        this.index = buffer.readInt();
        this.up = buffer.readBoolean();
    }

    @Override
    protected void handle(Player player, Level world, ItemStack targetDevice){
        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(this.index < 0 || this.index > targets.size() - 1)
            return;
        TargetDeviceItem.moveTarget(targetDevice, this.index, this.up);
    }
}
