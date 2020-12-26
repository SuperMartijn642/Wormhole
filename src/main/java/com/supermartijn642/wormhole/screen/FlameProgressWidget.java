package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Supplier;

/**
 * Created 12/25/2020 by SuperMartijn642
 */
public class FlameProgressWidget extends Widget {

    private static final ResourceLocation FLAME = new ResourceLocation("wormhole", "textures/gui/progress_flame.png");

    private final Supplier<Float> progress;

    public FlameProgressWidget(Supplier<Float> progress, int x, int y, int width, int height){
        super(x, y, width, height, new StringTextComponent(""));
        this.progress = progress;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        Minecraft.getInstance().getTextureManager().bindTexture(FLAME);
        float progress = Math.max(Math.min(this.progress.get(), 1), 0);
        if(progress != 1)
            drawTexture(matrixStack, this.x, this.y, this.width, this.height * (1 - progress), 0, 0, 0.5f, (1 - progress));
        if(progress != 0)
            drawTexture(matrixStack, this.x, this.y + this.height * (1 - progress), this.width, this.height * progress, 0.5f, 1 - progress, 0.5f, progress);
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
