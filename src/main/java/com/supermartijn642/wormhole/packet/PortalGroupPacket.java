package com.supermartijn642.wormhole.packet;

import com.supermartijn642.core.network.BlockPosBasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class PortalGroupPacket extends BlockPosBasePacket {

    public PortalGroupPacket(PortalGroup group){
        this.pos = group.shape.frame.get(0);
    }

    public PortalGroupPacket(){
    }

    @Override
    protected void handle(BlockPos pos, PacketContext context){
        EntityPlayer player = context.getSendingPlayer();
        if(player == null || player.getPositionVector().squareDistanceTo(this.pos.getX(), this.pos.getY(), this.pos.getZ()) > 100 * 100)
            return;
        World level = context.getWorld();
        if(level == null)
            return;
        PortalGroupCapability groups = level.getCapability(PortalGroupCapability.CAPABILITY, null);
        if(groups == null)
            return;
        PortalGroup group = groups.getGroup(this.pos);
        if(group == null)
            return;
        this.handle(group, context);
    }

    protected abstract void handle(PortalGroup group, PacketContext context);
}
