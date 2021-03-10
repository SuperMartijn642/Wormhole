package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.getDefaultState();

        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.translated(x, y, 350);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scaled(scale, scale, scale);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderHelper.setupGuiFlatDiffuseLighting();

        MatrixStack matrixstack = new MatrixStack();
        matrixstack.rotate(new Quaternion(pitch, yaw, 0, true));

        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
        IModelData modelData = EmptyModelData.INSTANCE;

        matrixstack.translate(-0.5, -0.5, -0.5);
        RenderType rendertype = RenderType.getTranslucent();
        renderModel(model, state, matrixstack, renderTypeBuffer.getBuffer(rendertype), modelData);

        renderTypeBuffer.finish();
        RenderSystem.enableDepthTest();
        RenderHelper.setupGui3DDiffuseLighting();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    private static void renderModel(IBakedModel modelIn, BlockState state, MatrixStack matrixStackIn, IVertexBuilder bufferIn, IModelData modelData){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData));
    }

    private static void renderQuads(MatrixStack matrixStackIn, IVertexBuilder bufferIn, List<BakedQuad> quadsIn){
        MatrixStack.Entry matrix = matrixStackIn.getLast();

        for(BakedQuad bakedquad : quadsIn)
            bufferIn.addVertexData(matrix, bakedquad, 1, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY, false);
    }

}
