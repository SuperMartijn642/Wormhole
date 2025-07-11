package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.wormhole.WormholeClient;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 11/12/2020 by SuperMartijn642
 */
public class PortalTargetEditColorButton extends AbstractButtonWidget {

    public static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath("wormhole", "gui/small_color_buttons");

    public boolean visible = true;
    private final Supplier<PortalTarget> target;
    private final Supplier<DyeColor> color;

    public PortalTargetEditColorButton(PortalGroupScreen screen, int x, int y, Supplier<Integer> targetIndex, Supplier<DyeColor> color, Runnable returnScreen){
        super(x, y, 10, 10, () -> {
            PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
            if(target != null)
                WormholeClient.openPortalTargetColorScreen(screen.pos, targetIndex.get(), returnScreen);
        });
        this.target = () -> screen.getPortalGroup().getTarget(targetIndex.get());
        this.color = color;
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(!this.visible)
            return;

        PortalTarget target = this.target.get();
        DyeColor color = this.color.get();
        float x = color == null ? 0 : (color.getId() + 1) * 8f / 136f;
        float y = target != null && this.isFocused() ? 0.5f : 0;
        graphics.submitSprite(BUTTONS, this.x, this.y, this.width, this.height, p -> p.uv(x, y, 8 / 136f, 0.5f));
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        if(this.visible)
            tooltips.accept(TextComponents.translation("wormhole.portal.gui.target_color").get());
    }

    @Override
    public Component getNarrationMessage(){
        return this.visible ? TextComponents.translation("wormhole.portal.gui.target_color").get() : null;
    }
}






















