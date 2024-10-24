package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 11/17/2020 by SuperMartijn642
 */
public class EnergyBarWidget extends AbstractButtonWidget {

    private static final ResourceLocation BARS = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/energy_bars.png");

    private final Supplier<Integer> energy, capacity;

    public EnergyBarWidget(int x, int y, int width, int height, Supplier<Integer> energy, Supplier<Integer> capacity){
        super(x, y, width, height, () -> EnergyFormat.cycleEnergyType(!Screen.hasShiftDown()));
        this.energy = energy;
        this.capacity = capacity;
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        ScreenUtils.drawTexture(BARS, context.poseStack(), this.x, this.y, this.width, this.height, this.isFocused() ? 1 / 11f : 0, 0, 1 / 11f, 1);
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        float percentage = capacity == 0 ? 1 : Math.max(Math.min(energy / (float)capacity, 1), 0);
        if(percentage != 0)
            ScreenUtils.drawTexture(BARS, context.poseStack(), this.x, this.y + this.height * (1 - percentage), this.width, this.height * percentage, 3 / 11f, 1 - percentage, 1 / 11f, percentage);
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        int energy = this.energy.get();
        int capacity = this.capacity.get();
        tooltips.accept(TextComponents.string(EnergyFormat.formatCapacityWithUnit(energy, capacity)).get());
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.string(EnergyFormat.formatCapacityWithUnit(this.energy.get(), this.capacity.get())).get();
    }
}
