package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public abstract class PortalGroupScreen extends BaseScreen {

    public final BlockPos pos;

    public PortalGroupScreen(String titleKey, BlockPos pos){
        super(new TranslationTextComponent(titleKey));
        this.pos = pos;
    }

    protected PortalGroup getPortalGroup(){
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return ((PortalGroupTile)tile).getGroup();
        this.closeScreen();
        return null;
    }

    protected <T> T getFromPortalGroup(Function<PortalGroup,T> function, T defaultValue){
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return function.apply(((PortalGroupTile)tile).getGroup());
        this.closeScreen();
        return defaultValue;
    }

    @Override
    public void tick(){
        if(!this.canInteractWith(ClientProxy.getPlayer())){
            this.closeScreen();
            return;
        }
        super.tick();
    }

    private boolean canInteractWith(PlayerEntity playerIn){
        return this.pos.distanceSq(playerIn.getPosition()) < 64 * 64;
    }
}
