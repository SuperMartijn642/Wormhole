package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class WormholeAbstractButton extends AbstractButton {

    private final Runnable pressable;

    public WormholeAbstractButton(int x, int y, int width, int height, String text, Runnable onPress){
        super(x, y, width, height, text);
        this.pressable = onPress;
    }

    @Override
    public void onPress(){
        if(this.pressable != null)
            this.pressable.run();
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks){
        this.renderButton(mouseX, mouseY);
    }

    protected abstract void renderButton(int mouseX, int mouseY);

    protected void drawTexture(float x, float y, float width, float height){
        drawTexture(x, y, width, height, 0, 0, 1, 1);
    }

    protected void drawTexture(float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        int z = this.getBlitOffset();
        GlStateManager.enableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, z).tex(tx, ty + theight).endVertex();
        buffer.pos(x + width, y + height, z).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(x + width, y, z).tex(tx + twidth, ty).endVertex();
        buffer.pos(x, y, z).tex(tx, ty).endVertex();
        tessellator.draw();
    }
}
