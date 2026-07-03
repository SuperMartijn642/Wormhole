package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    private static final BlockModelRenderState BLOCK_RENDER_STATE = new BlockModelRenderState();
    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    public static void drawBlock(PoseStack poseStack, SubmitNodeCollector output, Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(1, -1, -1);
        poseStack.scale((float)scale, (float)scale, (float)scale);

        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));

        poseStack.translate(-0.5, -0.5, -0.5);

        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = BLOCK_RENDER_STATE.setupModel(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
        RANDOM_SOURCE.setSeed(state.getSeed(BlockPos.ZERO));
        ModelData modelData = model.getModelData(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, ModelData.EMPTY);
        model.collectParts(RANDOM_SOURCE, parts, modelData);
        BLOCK_RENDER_STATE.submit(poseStack, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
