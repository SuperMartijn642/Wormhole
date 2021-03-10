package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Supplier;

/**
 * Created 11/12/2020 by SuperMartijn642
 */
public class PortalTargetEditColorButton extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/small_color_buttons.png");

    public boolean visible = true;
    private final Supplier<PortalTarget> target;
    private final Supplier<EnumDyeColor> color;

    public PortalTargetEditColorButton(PortalGroupScreen screen, int x, int y, Supplier<Integer> targetIndex, Supplier<EnumDyeColor> color, Runnable returnScreen){
        super(x, y, 10, 10, () -> {
            PortalTarget target = screen.getFromPortalGroup(group -> group.getTarget(targetIndex.get()), null);
            if(target != null)
                ClientProxy.openPortalTargetColorScreen(screen.pos, targetIndex.get(), returnScreen);
        });
        this.target = () -> screen.getFromPortalGroup(group -> group.getTarget(targetIndex.get()), null);
        this.color = color;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        if(!this.visible)
            return;

        PortalTarget target = this.target.get();
        Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTONS);
        EnumDyeColor color = this.color.get();
        float x = color == null ? 0 : (color.getMetadata() + 1) * 8f / 136f;
        float y = target != null && this.isHovered() ? 0.5f : 0;
        ScreenUtils.drawTexture(this.x, this.y, this.width, this.height, x, y, 8 / 136f, 0.5f);
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }

    @Override
    public ITextComponent getHoverText(){
        return this.visible ? new TextComponentTranslation("wormhole.portal.gui.target_color") : null;
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.visible ? new TextComponentTranslation("wormhole.portal.gui.target_color") : null;
    }
}






















