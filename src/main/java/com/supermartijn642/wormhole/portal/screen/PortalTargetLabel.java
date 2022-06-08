package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalTargetLabel extends WormholeLabel implements IHoverTextWidget {

    private final String hoverText;

    public PortalTargetLabel(PortalGroupScreen screen, Supplier<Integer> targetIndex, int x, int y, int width, int height, String hoverText, Function<PortalTarget,String> text, boolean translate){
        super(x, y, width, height, () -> {
            PortalTarget target = screen.getObject().getTarget(targetIndex.get());
            return target == null ? "" : text.apply(target);
        }, translate);
        this.hoverText = hoverText;
    }

    @Override
    public Component getHoverText(){
        return Component.translatable(this.hoverText);
    }
}
