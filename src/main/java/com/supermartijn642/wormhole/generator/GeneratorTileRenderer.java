package com.supermartijn642.wormhole.generator;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

import java.awt.*;

/**
 * Created 12/26/2020 by SuperMartijn642
 */
public class GeneratorTileRenderer<T extends GeneratorTile> implements BlockEntityRenderer<T> {

    protected final Color color;

    public GeneratorTileRenderer(Color color){
        this.color = color;
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){

    }
}
