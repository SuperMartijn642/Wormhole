package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.active){
            ScreenUtils.fillRect(matrixStack, this.x, this.y, this.width, this.height, -6250336);
            ScreenUtils.fillRect(matrixStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0xff404040);

            int enabledTextColor = 14737632;
//            int disabledTextColor = 7368816;
            String text = this.text.get();
            FontRenderer font = Minecraft.getInstance().fontRenderer;
            TextComponent textComponent = this.translate ? new TranslationTextComponent(text) : new StringTextComponent(text);
            int width = font.getStringPropertyWidth(textComponent);
            font.func_243248_b(matrixStack, textComponent, this.x + (this.width - width) / 2f, this.y + 2, enabledTextColor);
        }
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.translate ? new TranslationTextComponent(this.text.get()) : new StringTextComponent(this.text.get());
    }
}
