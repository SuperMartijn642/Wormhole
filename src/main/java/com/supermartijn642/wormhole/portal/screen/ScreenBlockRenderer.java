package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).pushFilter(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 350);
        GlStateManager.scaled(1, -1, 1);
        GlStateManager.scalef((float)scale, (float)scale, (float)scale);
        RenderHelper.turnOff();

        GlStateManager.rotated(pitch, 1, 0, 0);
        GlStateManager.rotated(yaw, 0, 1, 0);

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        IModelData modelData = EmptyModelData.INSTANCE;

        GlStateManager.translated(-0.5, -0.5, -0.5);
        renderModel(model, state, modelData);

        GlStateManager.popMatrix();
        GlStateManager.enableDepthTest();
        RenderHelper.turnOnGui();
    }

    private static void renderModel(IBakedModel model, BlockState state, IModelData modelData){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(buffer, model.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(buffer, model.getQuads(state, null, random, modelData));

        tessellator.end();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn){
        for(BakedQuad bakedquad : quadsIn)
            bufferIn.putBulkData(bakedquad.getVertices());
    }
}
