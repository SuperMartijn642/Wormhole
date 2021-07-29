package com.supermartijn642.wormhole.energycell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public class EnergyCellTileRenderer implements BlockEntityRenderer<EnergyCellTile> {

    public static final ResourceLocation[] ENERGY_CELL_MODELS = new ResourceLocation[16];

    static{
        for(int i = 0; i < 16; i++)
            ENERGY_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/energy_cell/basic/basic_energy_cell_" + i);
    }

    @Override
    public void render(EnergyCellTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
        ClientUtils.getBlockRenderer().getModelRenderer().renderModel(
            matrixStack.last(), buffer.getBuffer(RenderType.solid()), tile.getBlockState(), model, 1, 1, 1, combinedLight, combinedOverlay, EmptyModelData.INSTANCE
        );
    }

    public static BakedModel getModelForTile(EnergyCellTile tile){
        int texture = tile.getMaxEnergyStored(true) > 0 ? (int)Math.ceil((double)tile.getEnergyStored(true) / tile.getMaxEnergyStored(true) * 15) : 0;
        return Minecraft.getInstance().getModelManager().getModel(ENERGY_CELL_MODELS[texture]);
    }
}
