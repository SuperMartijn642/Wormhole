package com.supermartijn642.wormhole.generator;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import java.awt.*;

/**
 * Created 12/26/2020 by SuperMartijn642
 */
public class GeneratorTileRenderer<T extends GeneratorTile> extends TileEntitySpecialRenderer<T> {

    protected final Color color;

    public GeneratorTileRenderer(Color color){
        this.color = color;
    }

    @Override
    public void render(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
    }
}
