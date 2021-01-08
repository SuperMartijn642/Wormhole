package com.supermartijn642.wormhole.targetcell;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public class TargetCellTileRenderer extends TileEntitySpecialRenderer<TargetCellTile> {

    public static final ModelResourceLocation[] BASIC_TARGET_CELL_MODELS = new ModelResourceLocation[5];
    public static final ModelResourceLocation[] ADVANCED_TARGET_CELL_MODELS = new ModelResourceLocation[9];

    static{
        for(int i = 0; i < 5; i++)
            BASIC_TARGET_CELL_MODELS[i] = new ModelResourceLocation("wormhole:basic_target_cell", "model=" + (i + 1));
        for(int i = 0; i < 9; i++)
            ADVANCED_TARGET_CELL_MODELS[i] = new ModelResourceLocation("wormhole:advanced_target_cell", "model=" + (i + 1));
    }

    @Override
    public void render(TargetCellTile tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        ModelResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        GlStateManager.disableLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(modelLocation);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        bufferBuilder.setTranslation(x - tile.getPos().getX(), y - tile.getPos().getY(), z - tile.getPos().getZ());

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
            tile.getWorld(), model, tile.getBlockState(), tile.getPos(), bufferBuilder, true
        );

        bufferBuilder.setTranslation(0, 0, 0);

        tessellator.draw();
    }

    public static IBakedModel getModelForTile(TargetCellTile tile){
        ModelResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(modelLocation);
    }
}
