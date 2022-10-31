package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
        GlStateManager.translate(x + width / 2, y + height / 2, 350);
        GlStateManager.scale(scale, -scale, scale);

        ScreenUtils.bindTexture(TextureAtlases.getBlocks());
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushAttrib();
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, (FloatBuffer)ByteBuffer.allocateDirect(16).asFloatBuffer().put(new float[]{0, 0, 0, 0}).flip());
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, (FloatBuffer)ByteBuffer.allocateDirect(16).asFloatBuffer().put(new float[]{1, 1, 1, 1}).flip());
        GL11.glDisable(GL11.GL_LIGHT1);

        GlStateManager.rotate(45, 1, 0, 0);
        GlStateManager.rotate((float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, 1, 0);
        GlStateManager.translate(-center.getX(), -center.getY(), -center.getZ());

        for(BlockPos pos : shape.frame)
            renderBlock(level, pos, true);
        for(BlockPos pos : shape.area){
            if(!level.isAirBlock(pos)){
                renderBlock(level, pos, level.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderBlockEntity(level, pos);
            }
        }

        GlStateManager.popAttrib();
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).restoreLastBlurMipmap();
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
    }

    private static void renderBlock(World level, BlockPos pos, boolean valid){
        IBlockState state = level.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderType() != EnumBlockRenderType.MODEL)
            return;

        TileEntity entity = level.getTileEntity(pos);

        IBakedModel model = ClientUtils.getBlockRenderer().getModelForState(state);

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, model, valid);

        GlStateManager.popMatrix();
    }

    private static void translateAndRenderModel(IBlockState state, IBakedModel model, boolean valid){
        GlStateManager.pushMatrix();

        GlStateManager.translate(-0.5D, -0.5D, -0.5D);
        renderModel(model, state, valid);

        GlStateManager.popMatrix();
    }

    private static void renderModel(IBakedModel model, IBlockState state, boolean valid){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(EnumFacing direction : EnumFacing.values()){
            random.setSeed(42L);
            renderQuads(buffer, model.getQuads(state, direction, 42), valid);
        }

        random.setSeed(42L);
        renderQuads(buffer, model.getQuads(state, null, 42), valid);

        tessellator.draw();
    }

    private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, boolean valid){
        for(BakedQuad bakedquad : quads){
            buffer.addVertexData(bakedquad.getVertexData());

            if(!valid){
                for(int i = 0; i < 4; i++)
                    buffer.putColorRGBA(buffer.getColorIndex(4 - i), 255, 128, 128, 200);
            }
        }
    }

    private static void renderBlockEntity(World level, BlockPos pos){
        TileEntity entity = level.getTileEntity(pos);

        if(entity != null){
            TileEntitySpecialRenderer<TileEntity> entityRenderer = TileEntityRendererDispatcher.instance.getRenderer(entity);

            if(entityRenderer != null){
                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                entityRenderer.render(entity, 0, 0, 0, ClientUtils.getMinecraft().getRenderPartialTicks(), -1, 1);

                GlStateManager.popMatrix();
            }
        }
    }
}
