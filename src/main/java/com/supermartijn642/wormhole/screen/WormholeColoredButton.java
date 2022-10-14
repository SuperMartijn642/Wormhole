package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeColoredButton extends ButtonWidget {

    private final ResourceLocation RED_BUTTONS = new ResourceLocation("wormhole", "textures/gui/red_buttons.png");
    private final ResourceLocation GREEN_BUTTONS = new ResourceLocation("wormhole", "textures/gui/green_buttons.png");

    private int color; // 1 is red, 2 is green, other is default
    private boolean visible = true;

    public WormholeColoredButton(int x, int y, int width, int height, ITextComponent text, Runnable onPress){
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
    public void render(int mouseX, int mouseY){
        if(this.visible){
            if(this.color == 0)
                super.render(mouseX, mouseY);
            else{
                ResourceLocation texture = this.color == 1 ? RED_BUTTONS : GREEN_BUTTONS;
                drawButtonBackground((float)this.x, (float)this.y, (float)this.width, (float)this.height, (float)(this.isActive() ? (this.isFocused() ? 5 : 0) : 10) / 15f, texture);
                ScreenUtils.drawCenteredStringWithShadow(ClientUtils.getFontRenderer(), this.getText(), (float)this.x + (float)this.width / 2.0F, (float)this.y + (float)this.height / 2.0F - 5.0F, this.isActive() ? -1 : Integer.MAX_VALUE);
            }
        }
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }

    public static void drawButtonBackground(float x, float y, float width, float height, float yOffset, ResourceLocation texture){
        ScreenUtils.bindTexture(texture);
        ScreenUtils.drawTexture(x, y, 2.0F, 2.0F, 0.0F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + width - 2.0F, y, 2.0F, 2.0F, 0.6F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + width - 2.0F, y + height - 2.0F, 2.0F, 2.0F, 0.6F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x, y + height - 2.0F, 2.0F, 2.0F, 0.0F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + 2.0F, y, width - 4.0F, 2.0F, 0.4F, yOffset, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(x + 2.0F, y + height - 2.0F, width - 4.0F, 2.0F, 0.4F, yOffset + 0.2F, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(x, y + 2.0F, 2.0F, height - 4.0F, 0.0F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(x + width - 2.0F, y + 2.0F, 2.0F, height - 4.0F, 0.6F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, 0.4F, yOffset + 0.13333334F, 0.2F, 0.06666667F);
    }
}
