package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Created 10/9/2020 by SuperMartijn642
 */
public class ArrowButton extends AbstractButtonWidget {

    public static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath("wormhole", "gui/arrow_buttons");

    private final boolean up;
    public boolean active = true;

    public ArrowButton(int x, int y, boolean up, Runnable onPress){
        super(x, y, 10, 5, onPress);
        this.up = up;
    }

    public ArrowButton(int x, int y, int width, int height, boolean up, Runnable onPress){
        super(x, y, width, height, onPress);
        this.up = up;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("wormhole.gui.arrow_button." + (this.up ? "up" : "down")).get());
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("wormhole.gui.arrow_button." + (up ? "up" : "down")).get();
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        float x = (this.active ? this.isFocused() ? 15 : 0 : 30) / 45f;
        float y = (this.up ? 0 : 8) / 16f;
        graphics.submitSprite(BUTTONS, this.x, this.y, this.width, this.height, p -> p.uv(x, y, 15 / 45f, 8 / 16f));
    }
}
