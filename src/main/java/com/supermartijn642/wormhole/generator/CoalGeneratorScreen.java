package com.supermartijn642.wormhole.generator;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.container.WormholeTileContainerScreen;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.FlameProgressWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorScreen extends WormholeTileContainerScreen<CoalGeneratorTile,CoalGeneratorContainer> {

    private final int WIDTH = 176, HEIGHT = 166;

    private final InventoryPlayer playerInventory;

    public CoalGeneratorScreen(CoalGeneratorContainer screenContainer, EntityPlayer player){
        super(screenContainer, Wormhole.coal_generator.getUnlocalizedName() + ".name");
        this.playerInventory = player.inventory;
    }

    @Override
    protected int sizeX(){
        return WIDTH;
    }

    @Override
    protected int sizeY(){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(CoalGeneratorTile tile){
        this.addWidget(new EnergyBarWidget(8, 17, 20, 52, () -> tile.energy, () -> tile.energyCapacity));
        this.addWidget(new FlameProgressWidget(tile::getProgress, 80, 35, 14, 14));
    }

    @Override
    protected void renderForeground(CoalGeneratorTile tile, int mouseX, int mouseY){
        super.renderForeground(tile, mouseX, mouseY);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, 72, 4210752);
    }

    @Override
    protected void renderTooltips(CoalGeneratorTile tile, int mouseX, int mouseY){

    }
}
