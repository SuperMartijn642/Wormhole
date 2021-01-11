package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.screen.WormholeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Function;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public abstract class PortalGroupScreen extends WormholeScreen {

    public final BlockPos pos;

    public PortalGroupScreen(String titleKey, BlockPos pos){
        super(titleKey);
        this.pos = pos;
    }

    protected PortalGroup getPortalGroup(){
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return ((PortalGroupTile)tile).getGroup();
        Minecraft.getInstance().player.closeScreen();
        return null;
    }

    protected <T> T getFromPortalGroup(Function<PortalGroup,T> function, T defaultValue){
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return function.apply(((PortalGroupTile)tile).getGroup());
        Minecraft.getInstance().player.closeScreen();
        return defaultValue;
    }

    @Override
    public void tick(){
        if(!this.canInteractWith(ClientProxy.getPlayer())){
            ClientProxy.getPlayer().closeScreen();
            return;
        }
        super.tick();
    }

    private boolean canInteractWith(PlayerEntity playerIn){
        return this.pos.distanceSq(playerIn.getPosition()) < 64 * 64;
    }
}
