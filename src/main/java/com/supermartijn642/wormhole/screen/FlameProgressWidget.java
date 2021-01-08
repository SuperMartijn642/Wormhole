package com.supermartijn642.wormhole.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Created 12/25/2020 by SuperMartijn642
 */
public class FlameProgressWidget extends WormholeWidget {

    private static final ResourceLocation FLAME = new ResourceLocation("wormhole", "textures/gui/progress_flame.png");

    private final Supplier<Float> progress;

    public FlameProgressWidget(Supplier<Float> progress, int x, int y, int width, int height){
        super(x, y, width, height);
        this.progress = progress;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        Minecraft.getMinecraft().getTextureManager().bindTexture(FLAME);
        float progress = Math.max(Math.min(this.progress.get(), 1), 0);
        if(progress != 1)
            drawTexture(this.x, this.y, this.width, this.height * (1 - progress), 0, 0, 0.5f, (1 - progress));
        if(progress != 0)
            drawTexture(this.x, this.y + this.height * (1 - progress), this.width, this.height * progress, 0.5f, 1 - progress, 0.5f, progress);
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
}
