package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 10/9/2020 by SuperMartijn642
 */
public class ArrowButton extends AbstractButtonWidget implements IHoverTextWidget {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/arrow_buttons.png");

    private final boolean up;

    public ArrowButton(int x, int y, boolean up, Runnable onPress){
        super(x, y, 10, 5, onPress);
        this.up = up;
    }

    public ArrowButton(int x, int y, int width, int height, boolean up, Runnable onPress){
        super(x, y, width, height, onPress);
        this.up = up;
    }

    @Override
    public ITextComponent getHoverText(){
        return this.active ? new TranslationTextComponent("wormhole.gui.arrow_button." + (this.up ? "up" : "down")) : null;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return new TranslationTextComponent("wormhole.gui.arrow_button." + (up ? "up" : "down"));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        Minecraft.getInstance().getTextureManager().bindTexture(BUTTONS);
        float x = (this.active ? this.hovered ? 15 : 0 : 30) / 45f;
        float y = (this.up ? 0 : 8) / 16f;
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, x, y, 15 / 45f, 8 / 16f);
    }
}
