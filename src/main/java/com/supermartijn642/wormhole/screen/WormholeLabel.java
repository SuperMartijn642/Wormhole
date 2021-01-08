package com.supermartijn642.wormhole.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Supplier;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public class WormholeLabel extends WormholeWidget {

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
            Gui.drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xff404040);

            int enabledTextColor = 14737632;
//            int disabledTextColor = 7368816;
            String text = this.text.get();
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            ITextComponent textComponent = this.translate ? new TextComponentTranslation(text) : new TextComponentString(text);
            String s = textComponent.getFormattedText();
            int width = font.getStringWidth(s);
            font.drawString(s, this.x + (this.width - width) / 2, this.y + 2, enabledTextColor);
        }
    }
}
