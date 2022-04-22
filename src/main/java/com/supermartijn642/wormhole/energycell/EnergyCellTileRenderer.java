package com.supermartijn642.wormhole.energycell;

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
public class EnergyCellTileRenderer extends TileEntityRenderer<EnergyCellTile> {

    public static final ResourceLocation[] ENERGY_CELL_MODELS = new ResourceLocation[16];

    static{
        for(int i = 0; i < 16; i++)
            ENERGY_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/energy_cell/basic/basic_energy_cell_" + i);
    }

    @Override
    public void render(EnergyCellTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;

        GlStateManager.disableLighting();
        Minecraft.getInstance().textureManager.bind(AtlasTexture.LOCATION_BLOCKS);

        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        bufferBuilder.offset(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModelSmooth(
            tile.getLevel(), model, tile.getBlockState(), tile.getBlockPos(), bufferBuilder, true, new Random(), 42L, EmptyModelData.INSTANCE
        );

        bufferBuilder.offset(0, 0, 0);

        tessellator.end();
    }

    public static IBakedModel getModelForTile(EnergyCellTile tile){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        return Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
    }
}
