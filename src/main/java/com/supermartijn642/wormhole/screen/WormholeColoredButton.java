package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeColoredButton extends WormholeButton {

    private final ResourceLocation RED_BUTTONS = new ResourceLocation("wormhole", "textures/gui/red_buttons.png");
    private final ResourceLocation GREEN_BUTTONS = new ResourceLocation("wormhole", "textures/gui/green_buttons.png");

    private int color; // 1 is red, 2 is green, other is default
    private boolean visible = true;

    public WormholeColoredButton(int x, int y, int width, int height, String textKey, Runnable onPress){
        super(x, y, width, height, textKey, onPress);
    }

    public void setColorWhite(){
        this.color = 0;
    }

    public void setColorRed(){
        this.color = 1;
    }

    public void setColorGreen(){
        this.color = 2;
    }

    public void setInvisible(){
        this.visible = false;
    }

    public void setVisible(){
        this.visible = true;
    }

    @Override
    protected ResourceLocation getButtonTexture(){
        return this.color == 1 ? RED_BUTTONS : this.color == 2 ? GREEN_BUTTONS : super.getButtonTexture();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.visible)
            super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }
}
