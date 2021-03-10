package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.portal.PortalShape;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
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

import java.util.List;

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

        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.pushAttrib();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate(x + width / 2, y + height / 2, 350);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(scale, scale, scale);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.rotate(45, 1, 0, 0);
        GlStateManager.rotate((float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, 1, 0);
        GlStateManager.translate(-center.getX(), -center.getY(), -center.getZ());

        for(BlockPos pos : shape.frame)
            renderBlock(world, pos, true);
        for(BlockPos pos : shape.area){
            if(!world.isAirBlock(pos)){
                renderBlock(world, pos, world.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderTileEntity(world, pos);
            }
        }

        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private static void renderBlock(World world, BlockPos pos, boolean valid){
        IBlockState state = world.getBlockState(pos);

        if(!(state.getBlock() instanceof EnergyCellBlock) && !(state.getBlock() instanceof TargetCellBlock) && state.getRenderType() != EnumBlockRenderType.MODEL)
            return;

        TileEntity tile = world.getTileEntity(pos);

        IBakedModel model =
            tile instanceof TargetCellTile ? TargetCellTileRenderer.getModelForTile((TargetCellTile)tile) :
                tile instanceof EnergyCellTile ? EnergyCellTileRenderer.getModelForTile((EnergyCellTile)tile) :
                    Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, model, valid);

        GlStateManager.popMatrix();
    }

    private static void translateAndRenderModel(IBlockState state, IBakedModel modelIn, boolean valid){
        GlStateManager.pushMatrix();

        GlStateManager.translate(-0.5D, -0.5D, -0.5D);
        renderModel(modelIn, state, valid);

        GlStateManager.popMatrix();
    }

    private static void renderModel(IBakedModel modelIn, IBlockState state, boolean valid){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(EnumFacing direction : EnumFacing.values())
            renderQuads(bufferbuilder, modelIn.getQuads(state, direction, 42L), valid);

        renderQuads(bufferbuilder, modelIn.getQuads(state, null, 42L), valid);

        tessellator.draw();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn, boolean valid){
        GlStateManager.color(valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, 0.5f);

        for(BakedQuad bakedquad : quadsIn){
            bufferIn.addVertexData(bakedquad.getVertexData());

            if(!valid){
                for(int i = 0; i < 4; i++)
                    bufferIn.putColorRGBA(bufferIn.getColorIndex(4 - i), 255, 128, 128, 200);
            }
        }
    }

    private static void renderTileEntity(World world, BlockPos pos){
        TileEntity tile = world.getTileEntity(pos);

        if(tile != null){
            TileEntitySpecialRenderer<TileEntity> tileRenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);

            if(tileRenderer != null){
                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                tileRenderer.render(tile, 0, 0, 0, Minecraft.getMinecraft().getRenderPartialTicks(), -1, 1);

                GlStateManager.popMatrix();
            }
        }
    }
}
