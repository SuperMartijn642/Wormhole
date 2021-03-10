package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
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
        BlockState state = block.getDefaultState();

        GlStateManager.pushMatrix();
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.pushLightingAttributes();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translatef((float)x, (float)y, 350);
        GlStateManager.scalef(1.0F, -1.0F, 1.0F);
        GlStateManager.scaled(scale, scale, scale);

        GlStateManager.rotated(pitch, 1, 0, 0);
        GlStateManager.rotated(yaw, 0, 1, 0);

        RenderHelper.disableStandardItemLighting();

        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
        IModelData modelData = EmptyModelData.INSTANCE;

        GlStateManager.translated(-0.5, -0.5, -0.5);
        renderModel(model, state, modelData);

        GlStateManager.enableDepthTest();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.popAttributes();
    }

    private static void renderModel(IBakedModel modelIn, BlockState state, IModelData modelData){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(bufferbuilder, modelIn.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(bufferbuilder, modelIn.getQuads(state, null, random, modelData));

        tessellator.draw();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn){
        for(BakedQuad bakedquad : quadsIn)
            bufferIn.addVertexData(bakedquad.getVertexData());
    }

}
