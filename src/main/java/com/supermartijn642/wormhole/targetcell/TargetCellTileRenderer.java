package com.supermartijn642.wormhole.targetcell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Created 12/7/2020 by SuperMartijn642
 */
public class TargetCellTileRenderer implements BlockEntityRenderer<TargetCellTile> {

    public static final ResourceLocation[] BASIC_TARGET_CELL_MODELS = new ResourceLocation[5];
    public static final ResourceLocation[] ADVANCED_TARGET_CELL_MODELS = new ResourceLocation[9];

    static{
        for(int i = 0; i < 5; i++)
            BASIC_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/basic_target_cell_" + i);
        for(int i = 0; i < 9; i++)
            ADVANCED_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/advanced_target_cell_" + i);
    }

    @Override
    public void render(TargetCellTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        ResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        for(RenderType renderType : model.getRenderTypes(tile.getBlockState(), RandomSource.create(42), ModelData.EMPTY))
            ClientUtils.getBlockRenderer().getModelRenderer().renderModel(
                matrixStack.last(), buffer.getBuffer(renderType), tile.getBlockState(), model, 1, 1, 1, combinedLight, combinedOverlay, ModelData.EMPTY, renderType
            );
    }

    public static BakedModel getModelForTile(TargetCellTile tile){
        ResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }
}
