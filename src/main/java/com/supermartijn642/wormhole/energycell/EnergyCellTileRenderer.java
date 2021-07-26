package com.supermartijn642.wormhole.energycell;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

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

    public EnergyCellTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    public void render(EnergyCellTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
            tile.getLevel(), model, tile.getBlockState(), tile.getBlockPos(), matrixStack, buffer.getBuffer(RenderType.solid()), true, new Random(), 42L, combinedOverlay, EmptyModelData.INSTANCE
        );
    }

    public static IBakedModel getModelForTile(EnergyCellTile tile){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        return Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
    }
}
