package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 20000;

    public static void drawPortal(PortalShape shape, float x, float y, float width, float height){ // TODO fix transparency
        Level world = ClientProxy.getWorld();
        float scale = Math.min(width, height) / ((float)shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        PoseStack matrixstack = new PoseStack();
        matrixstack.translate(x + width / 2, y + height / 2, 350);
        matrixstack.scale(1, -1, 1);
        matrixstack.scale(scale, scale, scale);
        MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Lighting.setupForFlatItems();

        matrixstack.mulPose(new Quaternion(45, (float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, true));
        matrixstack.translate(-center.x(), -center.y(), -center.z());

        for(BlockPos pos : shape.frame)
            renderBlock(world, pos, matrixstack, renderTypeBuffer, true);
        for(BlockPos pos : shape.area){
            if(!world.isEmptyBlock(pos)){
                renderBlock(world, pos, matrixstack, renderTypeBuffer, world.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderTileEntity(world, pos, matrixstack, renderTypeBuffer);
            }
        }

        renderTypeBuffer.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
    }

    private static void renderBlock(Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, boolean valid){
        BlockState state = world.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != RenderShape.MODEL)
            return;

        BlockEntity tile = world.getBlockEntity(pos);

        BakedModel model =
            tile instanceof TargetCellTile ? TargetCellTileRenderer.getModelForTile((TargetCellTile)tile) :
                tile instanceof EnergyCellTile ? EnergyCellTileRenderer.getModelForTile((EnergyCellTile)tile) :
                    Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        IModelData modelData = tile == null ? EmptyModelData.INSTANCE : tile.getModelData();
        modelData = model.getModelData(world, pos, state, modelData);

        matrixStack.pushPose();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, matrixStack, renderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY, model, modelData, valid);

        matrixStack.popPose();
    }

    private static void translateAndRenderModel(BlockState state, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, BakedModel modelIn, IModelData modelData, boolean valid){
        matrixStackIn.pushPose();

        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
        RenderType rendertype = RenderType.translucent();
        renderModel(modelIn, state, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn.getBuffer(rendertype), modelData, valid);

        matrixStackIn.popPose();
    }

    private static void renderModel(BakedModel modelIn, BlockState state, int combinedLightIn, int combinedOverlayIn, PoseStack matrixStackIn, VertexConsumer bufferIn, IModelData modelData, boolean valid){
        RandomSource random = RandomSource.create();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData), combinedLightIn, combinedOverlayIn, valid);
        }

        random.setSeed(42L);
        renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData), combinedLightIn, combinedOverlayIn, valid);
    }

    private static void renderQuads(PoseStack matrixStackIn, VertexConsumer bufferIn, List<BakedQuad> quadsIn, int combinedLightIn, int combinedOverlayIn, boolean valid){
        PoseStack.Pose matrix = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn)
            bufferIn.putBulkData(matrix, bakedquad, 1, valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, combinedLightIn, combinedOverlayIn, false);
    }

    private static void renderTileEntity(Level world, BlockPos pos, PoseStack matrixStack, MultiBufferSource renderTypeBuffer){
        BlockEntity tile = world.getBlockEntity(pos);

        if(tile != null){
            BlockEntityRenderer<BlockEntity> tileRenderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(tile);

            if(tileRenderer != null){
                matrixStack.pushPose();
                matrixStack.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                tileRenderer.render(tile, Minecraft.getInstance().getFrameTime(), matrixStack, renderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY);

                matrixStack.popPose();
            }
        }
    }
}
