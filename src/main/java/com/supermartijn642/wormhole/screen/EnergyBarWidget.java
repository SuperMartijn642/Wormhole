package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.wormhole.EnergyFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Supplier;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class EnergyBarWidget extends WormholeAbstractButton implements IHoverTextWidget {

    private static final ResourceLocation BARS = new ResourceLocation("wormhole", "textures/gui/energy_bars.png");

    private final Supplier<Integer> energy, capacity;

    public EnergyBarWidget(int x, int y, int width, int height, Supplier<Integer> energy, Supplier<Integer> capacity, Runnable onPress){
        super(x, y, width, height, "energy bar", onPress);
        this.energy = energy;
        this.capacity = capacity;
    }

    @Override
    protected void renderButton(MatrixStack matrixStack, int mouseX, int mouseY){
        Minecraft.getInstance().getTextureManager().bindTexture(BARS);
        drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.isHovered() ? 1 / 11f : 0, 0, 1 / 11f, 1);
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        float percentage = capacity == 0 ? 1 : energy / (float)capacity;
        if(percentage != 0)
            drawTexture(matrixStack, this.x, this.y + this.height * (1 - percentage), this.width, this.height * percentage, 3 / 11f, 1 - percentage, 1 / 11f, percentage);
    }

    @Override
    public ITextComponent getHoverText(){
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        return new StringTextComponent(EnergyFormat.formatCapacity(energy, capacity));
    }

    @Override
    public void onPress(){
        EnergyFormat.cycleEnergyType(!Screen.hasShiftDown());
    }
}
