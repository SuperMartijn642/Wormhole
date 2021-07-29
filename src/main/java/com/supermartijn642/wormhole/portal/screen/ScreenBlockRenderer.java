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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

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
        IModelData modelData = EmptyModelData.INSTANCE;

        matrixstack.translate(-0.5, -0.5, -0.5);
        RenderType rendertype = RenderType.translucent();
        renderModel(model, state, matrixstack, renderTypeBuffer.getBuffer(rendertype), modelData);

        renderTypeBuffer.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    private static void renderModel(BakedModel modelIn, BlockState state, PoseStack matrixStackIn, VertexConsumer bufferIn, IModelData modelData){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData));
        }

        random.setSeed(42L);
        renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData));
    }

    private static void renderQuads(PoseStack matrixStackIn, VertexConsumer bufferIn, List<BakedQuad> quadsIn){
        PoseStack.Pose matrix = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn)
            bufferIn.putBulkData(matrix, bakedquad, 1, 1, 1, 1, 15728880, OverlayTexture.NO_OVERLAY, false);
    }

}
