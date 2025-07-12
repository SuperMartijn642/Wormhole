package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeColoredButton extends ButtonWidget {

    public static final ResourceLocation RED_BUTTONS = ResourceLocation.fromNamespaceAndPath("wormhole", "gui/red_buttons");
    public static final ResourceLocation GREEN_BUTTONS = ResourceLocation.fromNamespaceAndPath("wormhole", "gui/green_buttons");

    private int color; // 1 is red, 2 is green, other is default
    private boolean visible = true;

    public WormholeColoredButton(int x, int y, int width, int height, Component text, Runnable onPress){
        super(x, y, width, height, text, onPress);
    }

    public void setColorWhite(){
        this.color = 0;
    }

    public void setColorRed(){
        this.color = 1;
    }

    public void setColorGreen(){
        this.color = 2;
    }

    public void setInvisible(){
        this.visible = false;
    }

    public void setVisible(){
        this.visible = true;
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(this.visible){
            if(this.color == 0)
                super.render(context, graphics, mouseX, mouseY);
            else{
                ResourceLocation texture = this.color == 1 ? RED_BUTTONS : GREEN_BUTTONS;
                drawButtonBackground(graphics, (float)this.x, (float)this.y, (float)this.width, (float)this.height, (float)(this.isActive() ? (this.isFocused() ? 5 : 0) : 10) / 15f, texture);
                graphics.submitText(this.getText(), (float)this.x + (float)this.width / 2.0F, (float)this.y + (float)this.height / 2.0F - 5.0F, p -> p.color(this.isActive() ? -1 : Integer.MAX_VALUE).shadow().centerHorizontally());
            }
        }
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }

    public static void drawButtonBackground(GuiGraphicsHelper graphics, float x, float y, float width, float height, float yOffset, ResourceLocation texture){
        graphics.submitSprite(texture, x, y, 2.0F, 2.0F, p -> p.uv(0.0F, yOffset, 0.4F, 0.13333334F));
        graphics.submitSprite(texture, x + width - 2.0F, y, 2.0F, 2.0F, p -> p.uv(0.6F, yOffset, 0.4F, 0.13333334F));
        graphics.submitSprite(texture, x + width - 2.0F, y + height - 2.0F, 2.0F, 2.0F, p -> p.uv(0.6F, yOffset + 0.2F, 0.4F, 0.13333334F));
        graphics.submitSprite(texture, x, y + height - 2.0F, 2.0F, 2.0F, p -> p.uv(0.0F, yOffset + 0.2F, 0.4F, 0.13333334F));
        graphics.submitSprite(texture, x + 2.0F, y, width - 4.0F, 2.0F, p -> p.uv(0.4F, yOffset, 0.2F, 0.13333334F));
        graphics.submitSprite(texture, x + 2.0F, y + height - 2.0F, width - 4.0F, 2.0F, p -> p.uv(0.4F, yOffset + 0.2F, 0.2F, 0.13333334F));
        graphics.submitSprite(texture, x, y + 2.0F, 2.0F, height - 4.0F, p -> p.uv(0.0F, yOffset + 0.13333334F, 0.4F, 0.06666667F));
        graphics.submitSprite(texture, x + width - 2.0F, y + 2.0F, 2.0F, height - 4.0F, p -> p.uv(0.6F, yOffset + 0.13333334F, 0.4F, 0.06666667F));
        graphics.submitSprite(texture, x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, p -> p.uv(0.4F, yOffset + 0.13333334F, 0.2F, 0.06666667F));
    }
}
