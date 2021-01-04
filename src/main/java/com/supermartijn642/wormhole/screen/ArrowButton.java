package com.supermartijn642.wormhole.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 10/9/2020 by SuperMartijn642
 */
public class ArrowButton extends WormholeAbstractButton implements IHoverTextWidget {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/arrow_buttons.png");

    private final boolean up;

    public ArrowButton(int x, int y, boolean up, Runnable onPress){
        super(x, y, 10, 5, "wormhole.gui.arrow_button." + (up ? "up" : "down"), onPress);
        this.up = up;
    }

    @Override
    protected void renderButton(int mouseX, int mouseY){
        Minecraft.getInstance().getTextureManager().bindTexture(BUTTONS);
        float x = (this.active ? this.isHovered ? 15 : 0 : 30) / 45f;
        float y = (this.up ? 0 : 8) / 16f;
        drawTexture(this.x, this.y, this.width, this.height, x, y, 15 / 45f, 8 / 16f);
    }

    @Override
    public ITextComponent getHoverText(){
        return this.active ? new TranslationTextComponent("wormhole.gui.arrow_button." + (this.up ? "up" : "down")) : null;
    }
}
