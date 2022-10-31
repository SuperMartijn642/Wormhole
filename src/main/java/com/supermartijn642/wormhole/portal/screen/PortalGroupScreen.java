package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.widget.ObjectBaseWidget;
import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.portal.PortalGroup;
import net.minecraft.util.math.BlockPos;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public abstract class PortalGroupScreen extends ObjectBaseWidget<PortalGroup> {

    public final BlockPos pos;

    public PortalGroupScreen(int width, int height, BlockPos pos){
        super(0, 0, width, height, true);
        this.pos = pos;
    }

    @Override
    protected PortalGroup getObject(PortalGroup oldObject){
        PortalGroupCapability groups = ClientUtils.getWorld().getCapability(PortalGroupCapability.CAPABILITY, null);
        return groups == null ? null : groups.getGroup(this.pos);
    }

    @Override
    protected boolean validateObject(PortalGroup object){
        return object != null;
    }

    public PortalGroup getPortalGroup(){
        return this.object;
    }
}
