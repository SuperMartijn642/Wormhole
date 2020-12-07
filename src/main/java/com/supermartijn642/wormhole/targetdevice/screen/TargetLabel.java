package com.supermartijn642.wormhole.targetdevice.screen;

import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.screen.IHoverTextWidget;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import com.supermartijn642.wormhole.targetdevice.ITargetProvider;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

/**
 * Created 10/28/2020 by SuperMartijn642
 */
public class TargetLabel extends WormholeLabel implements IHoverTextWidget {

    private final String hoverText;

    public TargetLabel(ITargetProvider targets, int targetIndex, int x, int y, int width, int height, String hoverText, Function<PortalTarget,String> text, boolean translate){
        super(x, y, width, height, "wormhole.gui.label", () -> targets.getFromTargets(list -> list.size() > targetIndex ? text.apply(list.get(targetIndex)) : "", ""), translate);
        this.hoverText = hoverText;
    }

    @Override
    public ITextComponent getHoverText(){
        return new TranslationTextComponent(this.hoverText);
    }
}
