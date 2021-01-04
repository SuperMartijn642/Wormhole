package com.supermartijn642.wormhole.targetcell;

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
public class TargetCellTileRenderer extends TileEntityRenderer<TargetCellTile> {

    public static final ResourceLocation[] BASIC_TARGET_CELL_MODELS = new ResourceLocation[5];
    public static final ResourceLocation[] ADVANCED_TARGET_CELL_MODELS = new ResourceLocation[9];

    static{
        for(int i = 0; i < 5; i++)
            BASIC_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/basic_target_cell_" + i);
        for(int i = 0; i < 9; i++)
            ADVANCED_TARGET_CELL_MODELS[i] = new ResourceLocation("wormhole", "block/target_cell/advanced_target_cell_" + i);
    }

    public TargetCellTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn){
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TargetCellTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        ResourceLocation modelLocation = null;

        double percent = (double)tile.getNonNullTargetCount() / tile.getTargetCapacity();
        if(tile.type == TargetCellType.BASIC)
            modelLocation = BASIC_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (BASIC_TARGET_CELL_MODELS.length - 1))];
        else if(tile.type == TargetCellType.ADVANCED)
            modelLocation = ADVANCED_TARGET_CELL_MODELS[(int)Math.ceil(Math.min(percent, 1) * (ADVANCED_TARGET_CELL_MODELS.length - 1))];

        IBakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
            tile.getWorld(), model, tile.getBlockState(), tile.getPos(), matrixStack, buffer.getBuffer(RenderType.getSolid()), true, new Random(), 42L, combinedOverlay, EmptyModelData.INSTANCE
        );
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
