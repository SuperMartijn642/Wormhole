package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.ObjectBaseScreen;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public abstract class PortalGroupScreen extends ObjectBaseScreen<PortalGroup> {

    public final BlockPos pos;

    public PortalGroupScreen(String titleKey, BlockPos pos){
        super(new TranslationTextComponent(titleKey));
        this.pos = pos;
    }

    @Override
    protected PortalGroup getObject(){
        TileEntity tile = Minecraft.getInstance().world.getTileEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return ((PortalGroupTile)tile).getGroup();
        return null;
    }

    protected <T> T getFromPortalGroup(Function<PortalGroup,T> function, T defaultValue){
        PortalGroup group = this.getObject();
        return group == null ? defaultValue : function.apply(group);
    }
}
