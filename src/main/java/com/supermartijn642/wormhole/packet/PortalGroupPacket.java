package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class PortalGroupPacket {

    protected BlockPos pos;

    public PortalGroupPacket(PortalGroup group){
        this.pos = group.shape.frame.get(0);
    }

    public PortalGroupPacket(FriendlyByteBuf buffer){
        this.decode(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
    }

    protected void decode(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        Player player = contextSupplier.get().getSender();
        if(player == null || player.position().distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) > 100 * 100)
            return;
        Level world = player.level;
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

    protected abstract void handle(Player player, Level world, PortalGroup group);

}
