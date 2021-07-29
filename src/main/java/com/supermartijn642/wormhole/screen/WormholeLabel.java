package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.active){
            ScreenUtils.fillRect(matrixStack, this.x, this.y, this.width, this.height, -6250336);
            ScreenUtils.fillRect(matrixStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0xff404040);

            int enabledTextColor = 14737632;
//            int disabledTextColor = 7368816;
            String text = this.text.get();
            Font font = Minecraft.getInstance().font;
            BaseComponent textComponent = this.translate ? new TranslatableComponent(text) : new TextComponent(text);
            int width = font.width(textComponent);
            font.draw(matrixStack, textComponent, this.x + (this.width - width) / 2f, this.y + 2, enabledTextColor);
        }
    }

    @Override
    protected Component getNarrationMessage(){
        return this.translate ? new TranslatableComponent(this.text.get()) : new TextComponent(this.text.get());
    }
}
