package com.supermartijn642.wormhole.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 10/15/2020 by SuperMartijn642
 */
public class WormholeButton extends AbstractButtonWidget {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/buttons.png");

    protected String textKey;

    public WormholeButton(int x, int y, int width, int height, String textKey, Runnable onPress){
        super(x, y, width, height, onPress);
        this.textKey = textKey;
    }

    public void setTextKey(String textKey){
        this.textKey = textKey;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(this.getButtonTexture());
        drawBackground(this.x, this.y, this.width, this.height, (this.active ? this.isHovered() ? 5 : 0 : 10) / 15f);
        ScreenUtils.drawCenteredString(Minecraft.getInstance().fontRenderer, new TranslationTextComponent(this.textKey), this.x + this.width / 2f, this.y + this.height / 2f - 4, this.active ? 0xFFFFFFFF : Integer.MAX_VALUE);
    }

    protected ResourceLocation getButtonTexture(){
        return BUTTONS;
    }

    protected void drawBackground(float x, float y, float width, float height, float yOffset){
        // corners
        ScreenUtils.drawTexture(x, y, 2, 2, 0, yOffset, 2 / 5f, 2 / 15f);
        ScreenUtils.drawTexture(x + width - 2, y, 2, 2, 3 / 5f, yOffset, 2 / 5f, 2 / 15f);
        ScreenUtils.drawTexture(x + width - 2, y + height - 2, 2, 2, 3 / 5f, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        ScreenUtils.drawTexture(x, y + height - 2, 2, 2, 0, yOffset + 3 / 15f, 2 / 5f, 2 / 15f);
        // edges
        ScreenUtils.drawTexture(x + 2, y, width - 4, 2, 2 / 5f, yOffset, 1 / 5f, 2 / 15f);
        ScreenUtils.drawTexture(x + 2, y + height - 2, width - 4, 2, 2 / 5f, yOffset + 3 / 15f, 1 / 5f, 2 / 15f);
        ScreenUtils.drawTexture(x, y + 2, 2, height - 4, 0, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        ScreenUtils.drawTexture(x + width - 2, y + 2, 2, height - 4, 3 / 5f, yOffset + 2 / 15f, 2 / 5f, 1 / 15f);
        // center
        ScreenUtils.drawTexture(x + 2, y + 2, width - 4, height - 4, 2 / 5f, yOffset + 2 / 15f, 1 / 5f, 1 / 15f);
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return new TranslationTextComponent(this.textKey);
    }
}
