package com.supermartijn642.wormhole.targetcell;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public class TargetCellTileRenderer extends TileEntityRenderer<TargetCellTile> {

    public static final ResourceLocation[] BASIC_TARGET_CELL_MODELS = new ResourceLocation[5];
    public static final ResourceLocation[] ADVANCED_TARGET_CELL_MODELS = new ResourceLocation[9];

    static{
        for(int i = 0; i < 5; i++)
            BASIC_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/basic_target_cell_" + i);
        for(int i = 0; i < 9; i++)
            ADVANCED_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/advanced_target_cell_" + i);
    }

    @Override
    public void render(TargetCellTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        ResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        GlStateManager.disableLighting();
        Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        bufferBuilder.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelSmooth(
            tile.getWorld(), model, tile.getBlockState(), tile.getPos(), bufferBuilder, true, new Random(), 42L, EmptyModelData.INSTANCE
        );

        bufferBuilder.setTranslation(0, 0, 0);

        tessellator.draw();
    }

    public static IBakedModel getModelForTile(TargetCellTile tile){
        ResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }
}
