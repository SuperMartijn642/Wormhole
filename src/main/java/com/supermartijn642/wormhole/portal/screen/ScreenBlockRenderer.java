package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

/**
 * Created 2/2/2021 by SuperMartijn642
 */
public class ScreenBlockRenderer {

    public static void drawBlock(PoseStack poseStack, Block block, double x, double y, double scale, float yaw, float pitch){
        BlockState state = block.defaultBlockState();

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(1, -1, -1);
        poseStack.scale((float)scale, (float)scale, (float)scale);
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();

        poseStack.mulPose(new Quaternionf().setAngleAxis(pitch / 180 * Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(yaw / 180 * Math.PI, 0, 1, 0));

        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);

        poseStack.translate(-0.5, -0.5, -0.5);
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state);
        ModelBlockRenderer.renderModel(poseStack.last(), bufferSource.getBuffer(renderType), model, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        bufferSource.endBatch();
        poseStack.popPose();
    }
}
