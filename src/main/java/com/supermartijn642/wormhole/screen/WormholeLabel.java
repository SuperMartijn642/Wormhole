package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class WormholeLabel extends Widget {

    private final Supplier<String> text;
    private final boolean translate;

    public WormholeLabel(int x, int y, int width, int height, Supplier<String> text, boolean translate){
        super(x, y, width, height);
        this.text = text;
        this.translate = translate;
    }

    public WormholeLabel(int x, int y, int width, int height, String text, boolean translate){
        this(x, y, width, height, () -> text, translate);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        if(this.active){
            ScreenUtils.fillRect(this.x, this.y, this.width, this.height, -6250336);
            ScreenUtils.fillRect(this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0xff404040);

            int enabledTextColor = 14737632;
//            int disabledTextColor = 7368816;
            String text = this.text.get();
            FontRenderer font = Minecraft.getInstance().fontRenderer;
            String s = this.translate ? I18n.format(text) : text;
            int width = font.getStringWidth(s);
            ScreenUtils.drawString(s, this.x + (this.width - width) / 2f, this.y + 2, enabledTextColor);
        }
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.translate ? new TranslationTextComponent(this.text.get()) : new StringTextComponent(this.text.get());
    }
}
