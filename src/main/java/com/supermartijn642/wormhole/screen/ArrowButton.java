package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
    public Component getHoverText(){
        return this.active ? Component.translatable("wormhole.gui.arrow_button." + (this.up ? "up" : "down")) : null;
    }

    @Override
    protected Component getNarrationMessage(){
        return Component.translatable("wormhole.gui.arrow_button." + (up ? "up" : "down"));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(BUTTONS);
        float x = (this.active ? this.hovered ? 15 : 0 : 30) / 45f;
        float y = (this.up ? 0 : 8) / 16f;
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, x, y, 15 / 45f, 8 / 16f);
    }
}
