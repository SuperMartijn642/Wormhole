package com.supermartijn642.wormhole.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeButton extends WormholeAbstractButton {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/buttons.png");

    private String textKey;

    public WormholeButton(int x, int y, int width, int height, String textKey, Runnable onPress){
        super(x, y, width, height, onPress);
        this.textKey = textKey;
    }

    public void setTextKey(String textKey){
        this.textKey = textKey;
    }

    @Override
    protected void renderButton(int mouseX, int mouseY){
        Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTONS);
        drawBackground(this.x, this.y, this.width, this.height, (this.active ? this.isHovered() ? 5 : 0 : 10) / 15f);
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, new TextComponentTranslation(this.textKey), this.x + this.width / 2, this.y + this.height / 2 - 5, this.active ? 0xFFFFFFFF : Integer.MAX_VALUE);
    }

    protected void drawBackground(float x, float y, float width, float height, float yOffset){
        // corners
        this.drawTexture(x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        this.drawTexture(x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        this.drawTexture(x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        this.drawTexture(x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        this.drawTexture(x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        this.drawTexture(x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        this.drawTexture(x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        this.drawTexture(x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        this.drawTexture(x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    protected void drawTexture(float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        float z = this.blitOffset;
        GlStateManager.color(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, z).tex(tx, ty + theight).endVertex();
        buffer.pos(x + width, y + height, z).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(x + width, y, z).tex(tx + twidth, ty).endVertex();
        buffer.pos(x, y, z).tex(tx, ty).endVertex();
        tessellator.draw();
    }

    public void drawCenteredString(FontRenderer fontRenderer, ITextComponent text, int x, int y, int color){
        String s = text.getFormattedText();
        fontRenderer.drawStringWithShadow(s, (float)(x - fontRenderer.getStringWidth(s) / 2), (float)y, color);
    }
}
