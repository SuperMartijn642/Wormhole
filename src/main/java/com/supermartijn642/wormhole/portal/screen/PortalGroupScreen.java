package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.ObjectBaseScreen;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Function;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public abstract class PortalGroupScreen extends ObjectBaseScreen<PortalGroup> {

    public final BlockPos pos;

    public PortalGroupScreen(String titleKey, BlockPos pos){
        super(new TranslatableComponent(titleKey));
        this.pos = pos;
    }

    @Override
    protected PortalGroup getObject(){
        BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(this.pos);
        if(tile instanceof PortalGroupTile && ((PortalGroupTile)tile).hasGroup())
            return ((PortalGroupTile)tile).getGroup();
        return null;
    }

    protected <T> T getFromPortalGroup(Function<PortalGroup,T> function, T defaultValue){
        PortalGroup group = this.getObject();
        return group == null ? defaultValue : function.apply(group);
    }
}
