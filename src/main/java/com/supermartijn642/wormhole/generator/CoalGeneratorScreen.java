package com.supermartijn642.wormhole.generator;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BlockEntityBaseContainerWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.FlameProgressWidget;
import net.minecraft.network.chat.Component;

/**
 * Created 12/21/2020 by SuperMartijn642
 */
public class CoalGeneratorScreen extends BlockEntityBaseContainerWidget<CoalGeneratorBlockEntity,CoalGeneratorContainer> {

    private static final int WIDTH = 176, HEIGHT = 166;

    public CoalGeneratorScreen(){
        super(0, 0, WIDTH, HEIGHT, null, null);
    }

    @Override
    protected Component getNarrationMessage(CoalGeneratorBlockEntity object){
        return TextComponents.block(Wormhole.coal_generator).get();
    }

    @Override
    protected void addWidgets(CoalGeneratorBlockEntity entity){
        this.addWidget(new EnergyBarWidget(8, 17, 20, 52, () -> entity.energy, () -> entity.energyCapacity));
        this.addWidget(new FlameProgressWidget(entity::getProgress, 80, 35, 14, 14));
    }

    @Override
    protected void renderBackground(WidgetRenderContext context, int mouseX, int mouseY, CoalGeneratorBlockEntity object){
        ScreenUtils.drawScreenBackground(context.poseStack(), 0, 0, this.width(), this.height());
        super.renderBackground(context, mouseX, mouseY, object);
    }

    @Override
    protected CoalGeneratorBlockEntity getObject(CoalGeneratorBlockEntity oldObject){
        return this.container.getBlockEntity();
    }
}
