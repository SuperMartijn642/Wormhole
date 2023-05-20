package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 20000;

    public static void drawPortal(PoseStack poseStack, PortalShape shape, float x, float y, float width, float height){ // TODO fix transparency
        Level level = ClientUtils.getWorld();
        float scale = Math.min(width, height) / ((float)shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        poseStack.pushPose();
        poseStack.translate(x + width / 2, y + height / 2, 350);
        poseStack.scale(scale, -scale, scale);
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();

        RenderSystem.setShaderLights(Vector3f.YP, Vector3f.ZP);

        poseStack.mulPose(new Quaternion(45, (float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, true));
        poseStack.translate(-center.x(), -center.y(), -center.z());

        for(BlockPos pos : shape.frame)
            renderBlock(level, pos, poseStack, bufferSource, true);
        for(BlockPos pos : shape.area){
            if(!level.isEmptyBlock(pos)){
                renderBlock(level, pos, poseStack, bufferSource, level.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderBlockEntity(level, pos, poseStack, bufferSource);
            }
        }

        bufferSource.endBatch();
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    private static void renderBlock(Level level, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource, boolean valid){
        BlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != RenderShape.MODEL)
            return;

        BlockEntity entity = level.getBlockEntity(pos);

        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);

        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model, valid);

        poseStack.popPose();
    }

    private static void translateAndRenderModel(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, boolean valid){
        poseStack.pushPose();

        poseStack.translate(-0.5D, -0.5D, -0.5D);
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, true);
        renderModel(model, state, combinedLight, combinedOverlay, poseStack, bufferSource.getBuffer(renderType), renderType, valid);

        poseStack.popPose();
    }

    private static void renderModel(BakedModel model, BlockState state, int combinedLight, int combinedOverlay, PoseStack poseStack, VertexConsumer buffer, RenderType renderType, boolean valid){
        RandomSource random = RandomSource.create();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(state, direction, random), combinedLight, combinedOverlay, valid);
        }

        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(state, null, random), combinedLight, combinedOverlay, valid);
    }

    private static void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, int combinedLight, int combinedOverlay, boolean valid){
        PoseStack.Pose matrix = poseStack.last();

        for(BakedQuad bakedquad : quads)
            buffer.putBulkData(matrix, bakedquad, 1, valid ? 1 : 0.5f, valid ? 1 : 0.5f, combinedLight, combinedOverlay);
    }

    private static void renderBlockEntity(Level level, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource){
        BlockEntity entity = level.getBlockEntity(pos);

        if(entity != null){
            BlockEntityRenderer<BlockEntity> entityRenderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);

            if(entityRenderer != null){
                poseStack.pushPose();
                poseStack.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                entityRenderer.render(entity, ClientUtils.getMinecraft().getFrameTime(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
        }
    }
}
