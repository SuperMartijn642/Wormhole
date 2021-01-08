package com.supermartijn642.wormhole.energycell;

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
public class EnergyCellTileRenderer extends TileEntitySpecialRenderer<EnergyCellTile> {

    public static final ModelResourceLocation[] ENERGY_CELL_MODELS = new ModelResourceLocation[16];

    static{
        for(int i = 0; i < 16; i++)
            ENERGY_CELL_MODELS[i] = new ModelResourceLocation("wormhole:basic_energy_cell", "model=" + (i + 1));
    }

    @Override
    public void render(EnergyCellTile tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;

        GlStateManager.disableLighting();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);

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

    public static IBakedModel getModelForTile(EnergyCellTile tile){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
    }
}
