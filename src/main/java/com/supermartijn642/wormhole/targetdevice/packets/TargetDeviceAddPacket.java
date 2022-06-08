package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceAddPacket extends TargetDevicePacket {

    private String name;
    private BlockPos pos;
    private float yaw;

    public TargetDeviceAddPacket(InteractionHand hand, String name, BlockPos pos, float yaw){
        super(hand);
        this.name = name;
        this.pos = pos;
        this.yaw = yaw;
    }

    public TargetDeviceAddPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeUtf(this.name);
        buffer.writeBlockPos(this.pos);
        buffer.writeFloat(this.yaw);
    }

    @Override
    protected void decodeBuffer(FriendlyByteBuf buffer){
        super.decodeBuffer(buffer);
        this.name = buffer.readUtf(32767).trim();
        this.pos = buffer.readBlockPos();
        this.yaw = buffer.readFloat();
    }

    @Override
    protected void handle(Player player, Level world, ItemStack targetDevice){
        if(this.name.isEmpty())
            return;

        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(targets.size() < TargetDeviceItem.getMaxTargetCount(targetDevice))
            TargetDeviceItem.addTarget(targetDevice, new PortalTarget(world, this.pos, this.yaw, this.name));
    }
}
