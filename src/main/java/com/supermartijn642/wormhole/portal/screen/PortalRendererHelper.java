package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 30000;

    private static final RandomSource RANDOM = RandomSource.create();
    private static final CameraRenderState DUMMY_CAMERA_RENDER_STATE = new CameraRenderState();
    private static FeatureRenderDispatcher featureRenderDispatcher;

    public static void drawPortal(PoseStack poseStack, PortalShape shape, float x, float y, float width, float height){ // TODO fix transparency
        Level level = ClientUtils.getWorld();
        float scale = Math.min(width, height) / ((float)shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        poseStack.pushPose();
        poseStack.translate(x + width / 2, y + height / 2, 0);
        poseStack.scale(scale, -scale, -scale);
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();

        poseStack.mulPose(new Quaternionf().setAngleAxis(Math.PI / 4, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis((double)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 2 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x(), -center.y(), -center.z());

        setupFeatureRenderer(bufferSource);

        for(BlockPos pos : shape.frame)
            renderBlock(level, pos, poseStack, bufferSource, true);
        for(BlockPos pos : shape.area){
            if(!level.isEmptyBlock(pos)){
                renderBlock(level, pos, poseStack, bufferSource, level.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderBlockEntity(level, pos, poseStack, bufferSource);
            }
        }

        featureRenderDispatcher.renderAllFeatures();

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static void renderBlock(Level level, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource, boolean valid){
        BlockState state = level.getBlockState(pos);
        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != RenderShape.MODEL)
            return;

        BlockEntity entity = level.getBlockEntity(pos);

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        ModelData modelData = entity == null ? ModelData.EMPTY : entity.getModelData();
        modelData = model.getModelData(level, pos, state, modelData);

        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        poseStack.translate(-0.5, -0.5, -0.5);

        for(ChunkSectionLayer layer : model.getRenderTypes(state, RANDOM, modelData))
            ModelBlockRenderer.renderModel(poseStack.last(), bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(layer)), model, valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, modelData, layer);

        poseStack.popPose();
    }

    private static void renderBlockEntity(Level level, BlockPos pos, PoseStack poseStack, MultiBufferSource bufferSource){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity == null)
            return;

        BlockEntityRenderer<BlockEntity,BlockEntityRenderState> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
        if(renderer == null)
            return;

        BlockEntityRenderState renderState = renderer.createRenderState();
        renderer.extractRenderState(entity, renderState, ClientUtils.getPartialTicks(), Vec3.ZERO, null);
        poseStack.pushPose();
        poseStack.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

        ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().submit(renderState, poseStack, featureRenderDispatcher.getSubmitNodeStorage(), DUMMY_CAMERA_RENDER_STATE);

        poseStack.popPose();
    }

    private static void setupFeatureRenderer(MultiBufferSource.BufferSource bufferSource){
        if(featureRenderDispatcher == null){
            featureRenderDispatcher = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                ClientUtils.getBlockRenderer(),
                bufferSource,
                ClientUtils.getMinecraft().getAtlasManager(),
                new OutlineBufferSource() {
                    @Override
                    public VertexConsumer getBuffer(RenderType renderType){
                        return VertexMultiConsumer.create(new VertexConsumer[0]); // Discard everything
                    }
                },
                MultiBufferSource.immediate(ByteBufferBuilder.exactlySized(0)),
                ClientUtils.getFontRenderer()
            );
        }else
            featureRenderDispatcher.bufferSource = bufferSource;
    }
}
