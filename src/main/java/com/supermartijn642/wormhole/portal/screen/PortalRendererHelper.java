package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
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

import java.util.List;
import java.util.Random;

/**
 * Created 11/24/2020 by SuperMartijn642
 */
public class PortalRendererHelper {

    private static final int ROTATE_TIME = 20000;

    public static void drawPortal(PortalShape shape, float x, float y, float width, float height){
        World world = ClientProxy.getWorld();
        double scale = Math.min(width, height) / (shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.translatef(x + width / 2, y + height / 2, 350);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scaled(scale, scale, scale);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderHelper.setupForFlatItems();

        MatrixStack matrixstack = new MatrixStack();
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
        RenderHelper.setupFor3DItems();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    private static void renderBlock(World world, BlockPos pos, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, boolean valid){
        BlockState state = world.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != BlockRenderType.MODEL)
            return;

        TileEntity tile = world.getBlockEntity(pos);

        IBakedModel model =
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

    private static void translateAndRenderModel(BlockState state, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, IBakedModel modelIn, IModelData modelData, boolean valid){
        matrixStackIn.pushPose();

        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
        RenderType rendertype = RenderType.translucent();
        renderModel(modelIn, state, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn.getBuffer(rendertype), modelData, valid);

        matrixStackIn.popPose();
    }

    private static void renderModel(IBakedModel modelIn, BlockState state, int combinedLightIn, int combinedOverlayIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn, IModelData modelData, boolean valid){
        Random random = new Random();

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, direction, random, modelData), combinedLightIn, combinedOverlayIn, valid);
        }

        random.setSeed(42L);
        renderQuads(matrixStackIn, bufferIn, modelIn.getQuads(state, null, random, modelData), combinedLightIn, combinedOverlayIn, valid);
    }

    private static void renderQuads(MatrixStack matrixStackIn, IVertexBuilder bufferIn, List<BakedQuad> quadsIn, int combinedLightIn, int combinedOverlayIn, boolean valid){
        MatrixStack.Entry matrix = matrixStackIn.last();

        for(BakedQuad bakedquad : quadsIn)
            bufferIn.addVertexData(matrix, bakedquad, 1, valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, combinedLightIn, combinedOverlayIn, false);
    }

    private static void renderTileEntity(World world, BlockPos pos, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer){
        TileEntity tile = world.getBlockEntity(pos);

        if(tile != null){
            TileEntityRenderer<TileEntity> tileRenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);

            if(tileRenderer != null){
                matrixStack.pushPose();
                matrixStack.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                tileRenderer.render(tile, Minecraft.getInstance().getFrameTime(), matrixStack, renderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY);

                matrixStack.popPose();
            }
        }
    }
}
