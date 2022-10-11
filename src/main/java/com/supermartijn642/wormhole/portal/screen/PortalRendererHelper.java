package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 20000;

    public static void drawPortal(MatrixStack poseStack, PortalShape shape, float x, float y, float width, float height){ // TODO fix transparency
        World level = ClientUtils.getWorld();
        float scale = Math.min(width, height) / ((float)shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        poseStack.pushPose();
        poseStack.translate(x + width / 2, y + height / 2, 350);
        poseStack.scale(scale, -scale, scale);
        IRenderTypeBuffer.Impl bufferSource = RenderUtils.getMainBufferSource();

        RenderHelper.setupForFlatItems();
        RenderSystem.pushLightingAttributes();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, new float[]{0,0,0,0});
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, new float[]{1,1,1,1});
        GL11.glDisable(GL11.GL_LIGHT1);

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
        RenderSystem.popAttributes();
        poseStack.popPose();
        RenderSystem.enableDepthTest();
        RenderHelper.setupFor3DItems();
    }

    private static void renderBlock(World level, BlockPos pos, MatrixStack poseStack, IRenderTypeBuffer bufferSource, boolean valid){
        BlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != BlockRenderType.MODEL)
            return;

        TileEntity entity = level.getBlockEntity(pos);

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        IModelData modelData = entity == null ? EmptyModelData.INSTANCE : entity.getModelData();
        modelData = model.getModelData(level, pos, state, modelData);

        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model, modelData, valid);

        poseStack.popPose();
    }

    private static void translateAndRenderModel(BlockState state, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay, IBakedModel model, IModelData modelData, boolean valid){
        poseStack.pushPose();

        poseStack.translate(-0.5D, -0.5D, -0.5D);
        RenderType renderType = RenderTypeLookup.getRenderType(state, true);
        renderModel(model, state, combinedLight, combinedOverlay, poseStack, bufferSource.getBuffer(renderType), modelData, renderType, valid);

        poseStack.popPose();
    }

    private static void renderModel(IBakedModel model, BlockState state, int combinedLight, int combinedOverlay, MatrixStack poseStack, IVertexBuilder buffer, IModelData modelData, RenderType renderType, boolean valid){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(state, direction, random, modelData), combinedLight, combinedOverlay, valid);
        }

        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(state, null, random, modelData), combinedLight, combinedOverlay, valid);
    }

    private static void renderQuads(MatrixStack poseStack, IVertexBuilder buffer, List<BakedQuad> quads, int combinedLight, int combinedOverlay, boolean valid){
        MatrixStack.Entry matrix = poseStack.last();

        for(BakedQuad bakedquad : quads)
            buffer.addVertexData(matrix, bakedquad, 1f, valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, combinedLight, combinedOverlay, false);
    }

    private static void renderBlockEntity(World level, BlockPos pos, MatrixStack poseStack, IRenderTypeBuffer bufferSource){
        TileEntity entity = level.getBlockEntity(pos);

        if(entity != null){
            TileEntityRenderer<TileEntity> entityRenderer = TileEntityRendererDispatcher.instance.getRenderer(entity);

            if(entityRenderer != null){
                poseStack.pushPose();
                poseStack.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                entityRenderer.render(entity, ClientUtils.getMinecraft().getFrameTime(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
        }
    }
}
