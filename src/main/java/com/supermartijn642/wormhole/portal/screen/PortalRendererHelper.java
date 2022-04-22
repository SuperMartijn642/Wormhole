package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
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

    public static void drawPortal(PortalShape shape, float x, float y, float width, float height){
        World world = ClientProxy.getWorld();
        double scale = Math.min(width, height) / (shape.span + 1);
        Vector3f center = new Vector3f(
            (shape.maxCorner.getX() + shape.minCorner.getX()) / 2f,
            (shape.maxCorner.getY() + shape.minCorner.getY()) / 2f,
            (shape.maxCorner.getZ() + shape.minCorner.getZ()) / 2f
        );

        GlStateManager.pushMatrix();
        Minecraft.getInstance().textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).pushFilter(false, false);
        GlStateManager.pushLightingAttributes();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translatef(x + width / 2, y + height / 2, 350);
        GlStateManager.scalef(1.0F, -1.0F, 1.0F);
        GlStateManager.scaled(scale, scale, scale);

        RenderHelper.turnOff();

        GlStateManager.rotated(45, 1, 0, 0);
        GlStateManager.rotated((float)(System.currentTimeMillis() % ROTATE_TIME) / ROTATE_TIME * 360, 0, 1, 0);
        GlStateManager.translated(-center.x(), -center.y(), -center.z());

        for(BlockPos pos : shape.frame)
            renderBlock(world, pos, true);
        for(BlockPos pos : shape.area){
            if(!world.isEmptyBlock(pos)){
                renderBlock(world, pos, world.getBlockState(pos).getBlock() instanceof PortalBlock);
                renderTileEntity(world, pos);
            }
        }

        GlStateManager.enableDepthTest();
        RenderHelper.turnOnGui();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.popAttributes();
    }

    private static void renderBlock(World world, BlockPos pos, boolean valid){
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

        GlStateManager.pushMatrix();
        GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

        translateAndRenderModel(state, model, modelData, valid);

        GlStateManager.popMatrix();
    }

    private static void translateAndRenderModel(BlockState state, IBakedModel modelIn, IModelData modelData, boolean valid){
        GlStateManager.pushMatrix();

        GlStateManager.translated(-0.5D, -0.5D, -0.5D);
        renderModel(modelIn, state, modelData, valid);

        GlStateManager.popMatrix();
    }

    private static void renderModel(IBakedModel modelIn, BlockState state, IModelData modelData, boolean valid){
        Random random = new Random();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(Direction direction : Direction.values()){
            random.setSeed(42L);
            renderQuads(bufferbuilder, modelIn.getQuads(state, direction, random, modelData), valid);
        }

        random.setSeed(42L);
        renderQuads(bufferbuilder, modelIn.getQuads(state, null, random, modelData), valid);

        tessellator.end();
    }

    private static void renderQuads(BufferBuilder bufferIn, List<BakedQuad> quadsIn, boolean valid){
        GlStateManager.color4f(valid ? 1 : 0.5f, valid ? 1 : 0.5f, valid ? 1 : 0.8f, 0.5f);

        for(BakedQuad bakedquad : quadsIn){
            bufferIn.putBulkData(bakedquad.getVertices());

            if(!valid){
                for(int i = 0; i < 4; i++)
                    bufferIn.putColorRGBA(bufferIn.getStartingColorIndex(4 - i), 255, 128, 128, 200);
            }
        }
    }

    private static void renderTileEntity(World world, BlockPos pos){
        TileEntity tile = world.getBlockEntity(pos);

        if(tile != null){
            TileEntityRenderer<TileEntity> tileRenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);

            if(tileRenderer != null){
                GlStateManager.pushMatrix();
                GlStateManager.translated(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5);

                tileRenderer.render(tile, 0, 0, 0, Minecraft.getInstance().getFrameTime(), -1);

                GlStateManager.popMatrix();
            }
        }
    }
}
