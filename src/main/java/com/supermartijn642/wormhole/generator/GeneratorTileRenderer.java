package com.supermartijn642.wormhole.generator;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import java.awt.*;

/**
 * Created 12/26/2020 by SuperMartijn642
 */
public class GeneratorTileRenderer<T extends GeneratorTile> extends TileEntityRenderer<T> {

    protected final Color color;

    public GeneratorTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn, Color color){
        super(rendererDispatcherIn);
        this.color = color;
    }

    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){

    }
}
