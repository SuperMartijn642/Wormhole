package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        PoseStack matrixstack = new PoseStack();
        matrixstack.translate(x, y, 350);
        matrixstack.scale(1, -1, 1);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Lighting.setupForFlatItems();

        matrixstack.mulPose(new Quaternion(pitch, yaw, 0, true));

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        ModelData modelData = ModelData.EMPTY;

        matrixstack.translate(-0.5, -0.5, -0.5);
        for(RenderType renderType : model.getRenderTypes(state, RandomSource.create(42), modelData))
            renderModel(model, state, matrixstack, renderTypeBuffer.getBuffer(renderType), modelData, renderType);

        renderTypeBuffer.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    private static void renderModel(BakedModel modelIn, BlockState state, PoseStack matrixStackIn, VertexConsumer bufferIn, ModelData modelData, RenderType renderType){
        RandomSource random = RandomSource.create();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData, renderType));
        }

        random.setSeed(42L);
        renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData, renderType));
    }

    private static void renderQuads(PoseStack matrixStackIn, VertexConsumer bufferIn, List<BakedQuad> quadsIn){
        PoseStack.Pose matrix = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn)
            bufferIn.putBulkData(matrix, bakedquad, 1, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY, false);
    }

}
