package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

/**
 * Created 10/9/2020 by SuperMartijn642
 */
public class ArrowButton extends AbstractButtonWidget {

    private final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/arrow_buttons.png");

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
    protected void getTooltips(Consumer<ITextComponent> tooltips){
        tooltips.accept(TextComponents.translation("wormhole.gui.arrow_button." + (this.up ? "up" : "down")).get());
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return TextComponents.translation("wormhole.gui.arrow_button." + (up ? "up" : "down")).get();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY){
        ScreenUtils.bindTexture(BUTTONS);
        float x = (this.active ? this.isFocused() ? 15 : 0 : 30) / 45f;
        float y = (this.up ? 0 : 8) / 16f;
        ScreenUtils.drawTexture(poseStack, this.x, this.y, this.width, this.height, x, y, 15 / 45f, 8 / 16f);
    }
}
