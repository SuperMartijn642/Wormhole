package com.supermartijn642.wormhole.targetdevice.packets;

import com.supermartijn642.wormhole.packet.TargetDevicePacket;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.List;

/**
 * Created 10/25/2020 by SuperMartijn642
 */
public class TargetDeviceAddPacket extends TargetDevicePacket<TargetDeviceAddPacket> {

    private String name;
    private BlockPos pos;
    private float yaw;

    public TargetDeviceAddPacket(EnumHand hand, String name, BlockPos pos, float yaw){
        super(hand);
        this.name = name;
        this.pos = pos;
        this.yaw = yaw;
    }

    public TargetDeviceAddPacket(){
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, this.name);
        buffer.writeInt(this.pos.getX());
        buffer.writeInt(this.pos.getY());
        buffer.writeInt(this.pos.getZ());
        buffer.writeFloat(this.yaw);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        this.name = ByteBufUtils.readUTF8String(buffer);
        this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        this.yaw = buffer.readFloat();
    }

    @Override
    protected void handle(TargetDeviceAddPacket message, EntityPlayer player, World world, ItemStack targetDevice){
        if(message.name.isEmpty())
            return;

        List<PortalTarget> targets = TargetDeviceItem.getTargets(targetDevice);
        if(targets.size() < TargetDeviceItem.getMaxTargetCount(targetDevice))
            TargetDeviceItem.addTarget(targetDevice, new PortalTarget(world, message.pos, message.yaw, message.name));
    }
}
