package com.supermartijn642.wormhole.packet;

import com.supermartijn642.wormhole.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class WormholePacket<T extends WormholePacket> implements IMessage, IMessageHandler<T,IMessage> {

    public WormholePacket(){
    }

    @Override
    public void toBytes(ByteBuf buf){
    }

    @Override
    public void fromBytes(ByteBuf buf){
    }

    @Override
    public IMessage onMessage(T message, MessageContext ctx){
        EntityPlayer player = ctx.side == Side.SERVER ? ctx.getServerHandler().player : ClientProxy.getPlayer();
        if(player != null && player.world != null){
            if(ctx.side == Side.SERVER)
                player.getServer().addScheduledTask(() -> handle(message, player, player.world));
            else
                ClientProxy.queTask(() -> handle(message, player, player.world));
        }
        return null;
    }

    protected abstract void handle(T message, EntityPlayer player, World world);

}
