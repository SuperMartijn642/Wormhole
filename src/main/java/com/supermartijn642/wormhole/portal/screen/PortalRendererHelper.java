package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.extensions.RenderTypeExtension;
import com.supermartijn642.wormhole.portal.PortalShape;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 30000;

    private static final CameraRenderState DUMMY_CAMERA_RENDER_STATE = new CameraRenderState();
    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    /**
     * Copy of {@link Sheets.translucentBlockSheet()}
     */
    private static final RenderType INVALID_BLOCKS_RENDER_TYPE = RenderType.create(
        "wormhole_invalid_blocks",
        RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()
    );

    static{
        ((RenderTypeExtension)INVALID_BLOCKS_RENDER_TYPE).wormholeSetColorModulator(new Vector4f(0.8f, 0.5f, 0.5f, 0.8f));
    }

    public static void updateState(RenderState renderState, PortalShape shape){
        ClientLevel level = ClientUtils.getWorld();

        // Clear state
        renderState.framePositions.clear();
        renderState.frameBlockRenderStates.clear();
        renderState.frameEntityRenderStates.clear();
        renderState.areaPositions.clear();
        renderState.areaBlockRenderStates.clear();
        renderState.areaEntityRenderStates.clear();

        renderState.span = (float)shape.span;
        renderState.center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        // Frame
        List<BlockPos> frame = shape.frame;
        int index = -1;
        for(BlockPos pos : frame){
            BlockState state = level.getBlockState(pos);
            if(state.getRenderShape() == RenderShape.INVISIBLE)
                continue;

            index++;
            if(renderState.framePositions.size() == index)
                renderState.framePositions.add(pos);
            else
                renderState.framePositions.set(index, pos);

            // Block state model
            if(renderState.frameBlockRenderStates.size() == index)
                renderState.frameBlockRenderStates.add(new BlockModelRenderState());
            BlockModelRenderState blockRenderState = renderState.frameBlockRenderStates.get(index);
            blockRenderState.clear();
            ModelData modelData = level.getModelDataManager().getAtOrEmpty(pos);
            BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
            modelData = model.getModelData(level, pos, state, modelData);
            List<BlockStateModelPart> parts = blockRenderState.setupModel(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
            RANDOM_SOURCE.setSeed(state.getSeed(pos));
            model.collectParts(RANDOM_SOURCE, parts, modelData);
            IntList tintLayers = blockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(state))
                tintLayers.add(tintSource.colorInWorld(state, level, pos));

            // Block entity
            if(renderState.frameEntityRenderStates.size() == index)
                renderState.frameEntityRenderStates.add(null);
            else
                renderState.frameEntityRenderStates.set(index, null);
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity == null)
                continue;
            BlockEntityRenderer<BlockEntity,BlockEntityRenderState> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
            if(renderer == null)
                continue;
            BlockEntityRenderState entityRenderState = renderer.createRenderState();
            renderer.extractRenderState(entity, entityRenderState, ClientUtils.getPartialTicks(), Vec3.ZERO, null);
            renderState.frameEntityRenderStates.set(index, entityRenderState);
        }
        renderState.frameCount = index + 1;

        // Area
        List<BlockPos> area = shape.area;
        index = -1;
        for(BlockPos pos : area){
            BlockState state = level.getBlockState(pos);
            if(state.getRenderShape() == RenderShape.INVISIBLE)
                continue;

            index++;
            if(renderState.areaPositions.size() == index)
                renderState.areaPositions.add(pos);
            else
                renderState.areaPositions.set(index, pos);

            // Block state model
            if(renderState.areaBlockRenderStates.size() == index)
                renderState.areaBlockRenderStates.add(new BlockModelRenderState());
            BlockModelRenderState blockRenderState = renderState.areaBlockRenderStates.get(index);
            blockRenderState.clear();
            ModelData modelData = level.getModelDataManager().getAtOrEmpty(pos);
            BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
            modelData = model.getModelData(level, pos, state, modelData);
            List<BlockStateModelPart> parts = blockRenderState.setupModel(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
            RANDOM_SOURCE.setSeed(state.getSeed(pos));
            model.collectParts(RANDOM_SOURCE, parts, modelData);
            IntList tintLayers = blockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(state))
                tintLayers.add(tintSource.colorInWorld(state, level, pos));
            if(!(state.getBlock() instanceof PortalBlock))
                blockRenderState.renderType = INVALID_BLOCKS_RENDER_TYPE;

            // Block entity
            if(renderState.areaEntityRenderStates.size() == index)
                renderState.areaEntityRenderStates.add(null);
            else
                renderState.areaEntityRenderStates.set(index, null);
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity == null)
                continue;
            BlockEntityRenderer<BlockEntity,BlockEntityRenderState> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
            if(renderer == null)
                continue;
            BlockEntityRenderState entityRenderState = renderer.createRenderState();
            renderer.extractRenderState(entity, entityRenderState, ClientUtils.getPartialTicks(), Vec3.ZERO, null);
            renderState.areaEntityRenderStates.set(index, entityRenderState);
        }
        renderState.areaCount = index + 1;
    }

    public static void submitPortal(PoseStack poseStack, SubmitNodeCollector output, RenderState renderState, float width, float height){
        float scale = Math.min(width, height) / (renderState.span + 1);
        Vector3f center = renderState.center;

        // TODO currently there is no culling for neighboring blocks. Perhaps add custom block render state feature renderer that also allows for culling

        poseStack.pushPose();
        poseStack.translate(width / 2, height / 2, 0);
        poseStack.scale(scale, -scale, -scale);
        poseStack.mulPose(new Quaternionf().setAngleAxis(Math.PI / 4, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis((double)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 2 * Math.PI, 0, 1, 0));
        poseStack.translate(-center.x(), -center.y(), -center.z());

        // Frame
        BlockEntityRenderDispatcher blockEntityRenderDispatcher = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher();
        for(int i = 0; i < renderState.frameCount; i++){
            BlockPos pos = renderState.framePositions.get(i);
            poseStack.pushPose();
            poseStack.translate(pos.getX() - 0.5f, pos.getY() - 0.5f, pos.getZ() - 0.5f);
            BlockModelRenderState blockRenderState = renderState.frameBlockRenderStates.get(i);
            blockRenderState.submit(poseStack, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            BlockEntityRenderState entityRenderState = renderState.frameEntityRenderStates.get(i);
            if(entityRenderState != null)
                blockEntityRenderDispatcher.submit(entityRenderState, poseStack, output, DUMMY_CAMERA_RENDER_STATE);
            poseStack.popPose();
        }

        // Area
        for(int i = 0; i < renderState.areaCount; i++){
            BlockPos pos = renderState.areaPositions.get(i);
            poseStack.pushPose();
            poseStack.translate(pos.getX() - 0.5f, pos.getY() - 0.5f, pos.getZ() - 0.5f);
            BlockModelRenderState blockRenderState = renderState.areaBlockRenderStates.get(i);
            blockRenderState.submit(poseStack, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            BlockEntityRenderState entityRenderState = renderState.areaEntityRenderStates.get(i);
            if(entityRenderState != null)
                blockEntityRenderDispatcher.submit(entityRenderState, poseStack, output, DUMMY_CAMERA_RENDER_STATE);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    public static class RenderState {
        float span;
        Vector3f center;
        private int frameCount;
        private final List<BlockPos> framePositions = new ArrayList<>();
        private final List<BlockModelRenderState> frameBlockRenderStates = new ArrayList<>();
        private final List<BlockEntityRenderState> frameEntityRenderStates = new ArrayList<>();
        private int areaCount;
        private final List<BlockPos> areaPositions = new ArrayList<>();
        private final List<BlockModelRenderState> areaBlockRenderStates = new ArrayList<>();
        private final List<BlockEntityRenderState> areaEntityRenderStates = new ArrayList<>();
    }
}
