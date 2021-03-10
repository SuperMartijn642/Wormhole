package com.supermartijn642.wormhole.portal.screen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(Block block, double x, double y, double scale, float yaw, float pitch){
        IBlockState state = block.getDefaultState();

        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.pushAttrib();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate(x, y, 350);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.rotate(pitch, 1, 0, 0);
        GlStateManager.rotate(yaw, 0, 1, 0);

        RenderHelper.disableStandardItemLighting();

        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

        GlStateManager.translate(-0.5, -0.5, -0.5);
        renderModel(model, state);

        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private static void renderModel(IBakedModel modelIn, IBlockState state){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(EnumFacing direction : EnumFacing.values())
            renderQuads(bufferbuilder, modelIn.getQuads(state, direction, 42L));

        renderQuads(bufferbuilder, modelIn.getQuads(state, null, 42L));

        tessellator.draw();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn){
        for(BakedQuad bakedquad : quadsIn)
            bufferIn.addVertexData(bakedquad.getVertexData());
    }

}
