package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class EnergyBarWidget extends AbstractButtonWidget {

    private static final ResourceLocation BARS = new ResourceLocation("wormhole", "textures/gui/energy_bars.png");

    private final Supplier<Integer> energy, capacity;

    public EnergyBarWidget(int x, int y, int width, int height, Supplier<Integer> energy, Supplier<Integer> capacity){
        super(x, y, width, height, () -> EnergyFormat.cycleEnergyType(!Screen.hasShiftDown()));
        this.energy = energy;
        this.capacity = capacity;
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY){
        ScreenUtils.bindTexture(BARS);
        GlStateManager._enableAlphaTest();
        ScreenUtils.drawTexture(poseStack, this.x, this.y, this.width, this.height, this.isFocused() ? 1 / 11f : 0, 0, 1 / 11f, 1);
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        float percentage = capacity == 0 ? 1 : Math.max(Math.min(energy / (float)capacity, 1), 0);
        if(percentage != 0)
            ScreenUtils.drawTexture(poseStack, this.x, this.y + this.height * (1 - percentage), this.width, this.height * percentage, 3 / 11f, 1 - percentage, 1 / 11f, percentage);
    }

    @Override
    protected void getTooltips(Consumer<ITextComponent> tooltips){
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        tooltips.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, capacity)).get());
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return TextComponents.string(EnergyFormat.formatCapacityWithUnit(this.energy.get(), this.capacity.get())).get();
    }
}
