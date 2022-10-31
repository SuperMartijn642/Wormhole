package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(Block block, double x, double y, double scale, float yaw, float pitch){
        IBlockState state = block.getDefaultState();

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 350);
        GlStateManager.scale(1, -1, 1);
        GlStateManager.scale((float)scale, (float)scale, (float)scale);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.rotate(pitch, 1, 0, 0);
        GlStateManager.rotate(yaw, 0, 1, 0);

        IBakedModel model = ClientUtils.getBlockRenderer().getModelForState(state);

        GlStateManager.translate(-0.5, -0.5, -0.5);
        renderModel(model, state);

        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
    }

    private static void renderModel(IBakedModel model, IBlockState state){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(EnumFacing direction : EnumFacing.values()){
            random.setSeed(42L);
            renderQuads(buffer, model.getQuads(state, direction, 42));
        }

        random.setSeed(42L);
        renderQuads(buffer, model.getQuads(state, null, 42));

        tessellator.draw();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn){
        for(BakedQuad bakedquad : quadsIn)
            bufferIn.addVertexData(bakedquad.getVertexData());
    }
}
