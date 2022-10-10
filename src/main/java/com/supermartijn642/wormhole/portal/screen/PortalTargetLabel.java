package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.premade.LabelWidget;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalTargetLabel extends LabelWidget {

    private final String hoverText;

    public PortalTargetLabel(PortalGroupScreen screen, Supplier<Integer> targetIndex, int x, int y, int width, int height, String hoverText, Function<PortalTarget,String> text, boolean translate){
        super(x, y, width, height, () -> {
            PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
            return (target == null ? TextComponents.empty() : translate ? TextComponents.translation(text.apply(target)) : TextComponents.string(text.apply(target))).get();
        });
        this.hoverText = hoverText;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation(this.hoverText).get());
    }
}
