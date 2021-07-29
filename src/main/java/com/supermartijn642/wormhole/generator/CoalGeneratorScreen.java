package com.supermartijn642.wormhole.generator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.TileEntityBaseContainerScreen;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.FlameProgressWidget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorScreen extends TileEntityBaseContainerScreen<CoalGeneratorTile,CoalGeneratorContainer> {

    private final int WIDTH = 176, HEIGHT = 166;

    public CoalGeneratorScreen(CoalGeneratorContainer screenContainer, Inventory inv){
        super(screenContainer, new TranslatableComponent(Wormhole.coal_generator.getDescriptionId()));
    }

    @Override
    protected int sizeX(CoalGeneratorTile tile){
        return WIDTH;
    }

    @Override
    protected int sizeY(CoalGeneratorTile tile){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(CoalGeneratorTile tile){
        this.addWidget(new EnergyBarWidget(8, 17, 20, 52, () -> tile.energy, () -> tile.energyCapacity));
        this.addWidget(new FlameProgressWidget(tile::getProgress, 80, 35, 14, 14));
    }

    @Override
    protected void renderTooltips(PoseStack matrixStack, int i, int i1, CoalGeneratorTile coalGeneratorTile){
    }
}
