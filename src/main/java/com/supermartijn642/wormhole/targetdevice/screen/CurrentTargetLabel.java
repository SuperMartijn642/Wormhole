package com.supermartijn642.wormhole.targetdevice.screen;

import com.supermartijn642.wormhole.screen.IHoverTextWidget;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class CurrentTargetLabel extends WormholeLabel implements IHoverTextWidget {

    private final String hoverText;

    public CurrentTargetLabel(int x, int y, int width, int height, String title, String hoverText, Supplier<String> text, boolean translate){
        super(x, y, width, height, text, translate);
        this.hoverText = hoverText;
    }

    public CurrentTargetLabel(int x, int y, int width, int height, String title, String hoverText, String text, boolean translate){
        super(x, y, width, height, text, translate);
        this.hoverText = hoverText;
    }

    @Override
    public ITextComponent getHoverText(){
        return new TextComponentTranslation(this.hoverText);
    }
}
