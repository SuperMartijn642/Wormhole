package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeButton extends WormholeAbstractButton {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/buttons.png");

    private String textKey;

    public WormholeButton(int x, int y, int width, int height, String textKey, Runnable onPress){
        super(x, y, width, height, textKey, onPress);
        this.textKey = textKey;
    }

    public void setTextKey(String textKey){
        this.textKey = textKey;
    }

    @Override
    protected void renderButton(MatrixStack matrixStack, int mouseX, int mouseY){
        Minecraft.getInstance().getTextureManager().bindTexture(BUTTONS);
        drawBackground(matrixStack, this.x, this.y, this.width, this.height, (this.active ? this.isHovered() ? 5 : 0 : 10) / 15f);
        drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, new TranslationTextComponent(this.textKey), this.x + this.width / 2, this.y + this.height / 2 - 5, this.active ? 0xFFFFFFFF : Integer.MAX_VALUE);
    }

    protected void drawBackground(MatrixStack matrixStack, float x, float y, float width, float height, float yOffset){
        // corners
        this.drawTexture(matrixStack, x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        this.drawTexture(matrixStack, x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        this.drawTexture(matrixStack, x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        this.drawTexture(matrixStack, x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        this.drawTexture(matrixStack, x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        this.drawTexture(matrixStack, x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        this.drawTexture(matrixStack, x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        this.drawTexture(matrixStack, x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        this.drawTexture(matrixStack, x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    protected void drawTexture(MatrixStack matrixStack, float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        int z = this.getBlitOffset();
        Matrix4f matrix = matrixStack.getLast().getMatrix();

        GlStateManager.enableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(matrix, x, y + height, z).tex(tx, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y + height, z).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y, z).tex(tx + twidth, ty).endVertex();
        buffer.pos(matrix, x, y, z).tex(tx, ty).endVertex();
        tessellator.draw();
    }
}
