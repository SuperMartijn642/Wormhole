package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class PortalGroupPacket {

    protected BlockPos pos;

    public PortalGroupPacket(PortalGroup group){
        this.pos = group.shape.frame.get(0);
    }

    public PortalGroupPacket(PacketBuffer buffer){
        this.decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeBlockPos(this.pos);
    }

    protected void decode(PacketBuffer buffer){
        this.pos = buffer.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        PlayerEntity player = contextSupplier.get().getSender();
        if(player == null || player.getPositionVec().squareDistanceTo(this.pos.getX(), this.pos.getY(), this.pos.getZ()) > 100 * 100)
            return;
        World world = player.world;
        if(world == null)
            return;
        PortalGroupCapability groups = world.getCapability(PortalGroupCapability.CAPABILITY).orElse(null);
        if(groups == null)
            return;
        PortalGroup group = groups.getGroup(this.pos);
        if(group == null)
            return;
        contextSupplier.get().enqueueWork(() -> this.handle(player, world, group));
    }

    protected abstract void handle(PlayerEntity player, World world, PortalGroup group);

}
