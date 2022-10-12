package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(MatrixStack poseStack, Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        poseStack.pushPose();
        poseStack.translate(x, y, 350);
        poseStack.scale(1, -1, 1);
        poseStack.scale((float)scale, (float)scale, (float)scale);
        IRenderTypeBuffer.Impl bufferSource = RenderUtils.getMainBufferSource();
        RenderHelper.setupForFlatItems();

        poseStack.mulPose(new Quaternion(pitch, yaw, 0, true));

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        IModelData modelData = EmptyModelData.INSTANCE;

        poseStack.translate(-0.5, -0.5, -0.5);
        RenderType renderType = RenderTypeLookup.getRenderType(state);
        renderModel(model, state, poseStack, bufferSource.getBuffer(renderType), modelData, renderType);

        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        RenderHelper.setupFor3DItems();
    }

    private static void renderModel(IBakedModel model, BlockState state, MatrixStack poseStack, IVertexBuilder buffer, IModelData modelData, RenderType renderType){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(state, null, random, modelData));
    }

    private static void renderQuads(MatrixStack poseStack, IVertexBuilder buffer, List<BakedQuad> quads){
        MatrixStack.Entry matrix = poseStack.last();

        for(BakedQuad bakedquad : quads)
            buffer.addVertexData(matrix, bakedquad, 1, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY, false);
    }
}
