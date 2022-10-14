package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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

    public static void drawPortal(PortalShape shape, float x, float y, float width, float height){ // TODO fix transparency
        World level = ClientUtils.getWorld();
        float scale = Math.min(width, height) / ((float)shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        GlStateManager.pushMatrix();
        GlStateManager.translated(x + width / 2, y + height / 2, 350);
        GlStateManager.scalef(scale, -scale, scale);

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).pushFilter(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderHelper.turnOn();
        GlStateManager.pushLightingAttributes();
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, new float[]{0, 0, 0, 0});
        GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT, new float[]{1, 1, 1, 1});
        GL11.glDisable(GL11.GL_LIGHT1);

        GlStateManager.rotated(45, 1, 0, 0);
        GlStateManager.rotated((float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, 1, 0);
        GlStateManager.translated(-center.x(), -center.y(), -center.z());

        for(BlockPos pos : shape.frame)
            renderBlock(level, pos, true);
        for(BlockPos pos : shape.area){
            if(!level.isEmptyBlock(pos)){
                renderBlock(level, pos, level.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderBlockEntity(level, pos);
            }
        }

        GlStateManager.popAttributes();
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).popFilter();
        GlStateManager.popMatrix();
        GlStateManager.enableDepthTest();
        RenderHelper.turnOnGui();
    }

    private static void renderBlock(World level, BlockPos pos, boolean valid){
        BlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderShape() != BlockRenderType.MODEL)
            return;

        TileEntity entity = level.getBlockEntity(pos);

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        IModelData modelData = entity == null ? EmptyModelData.INSTANCE : entity.getModelData();
        modelData = model.getModelData(level, pos, state, modelData);

        GlStateManager.pushMatrix();
        GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, model, modelData, valid);

        GlStateManager.popMatrix();
    }

    private static void translateAndRenderModel(BlockState state, IBakedModel model, IModelData modelData, boolean valid){
        GlStateManager.pushMatrix();

        GlStateManager.translated(-0.5D, -0.5D, -0.5D);
        renderModel(model, state, modelData, valid);

        GlStateManager.popMatrix();
    }

    private static void renderModel(IBakedModel model, BlockState state, IModelData modelData, boolean valid){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(buffer, model.getQuads(state, direction, random, modelData), valid);
        }

        random.setSeed(42L);
        renderQuads(buffer, model.getQuads(state, null, random, modelData), valid);

        tessellator.end();
    }

    private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, boolean valid){
        for(BakedQuad bakedquad : quads){
            buffer.putBulkData(bakedquad.getVertices());

            if(!valid){
                for(int i = 0; i < 4; i++)
                    buffer.putColorRGBA(buffer.getStartingColorIndex(4 - i), 255, 128, 128, 200);
            }
        }
    }

    private static void renderBlockEntity(World level, BlockPos pos){
        TileEntity entity = level.getBlockEntity(pos);

        if(entity != null){
            TileEntityRenderer<TileEntity> entityRenderer = TileEntityRendererDispatcher.instance.getRenderer(entity);

            if(entityRenderer != null){
                GlStateManager.pushMatrix();
                GlStateManager.translated(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                entityRenderer.render(entity, 0, 0, 0, ClientUtils.getMinecraft().getFrameTime(), -1);

                GlStateManager.popMatrix();
            }
        }
    }
}
