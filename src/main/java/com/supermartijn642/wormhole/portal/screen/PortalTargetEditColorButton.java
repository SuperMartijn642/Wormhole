package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

/**
 * Created 11/12/2020 by SuperMartijn642
 */
public class PortalTargetEditColorButton extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation BUTTONS = new ResourceLocation("wormhole", "textures/gui/small_color_buttons.png");

    public boolean visible = true;
    private final Supplier<PortalTarget> target;
    private final Supplier<DyeColor> color;

    public PortalTargetEditColorButton(PortalGroupScreen screen, int x, int y, Supplier<Integer> targetIndex, Supplier<DyeColor> color, Runnable returnScreen){
        super(x, y, 10, 10, () -> {
            PortalTarget target = screen.getFromPortalGroup(group -> group.getTarget(targetIndex.get()), null);
            if(target != null)
                ClientProxy.openPortalTargetColorScreen(screen.pos, targetIndex.get(), returnScreen);
        });
        this.target = () -> screen.getFromPortalGroup(group -> group.getTarget(targetIndex.get()), null);
        this.color = color;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(!this.visible)
            return;

        PortalTarget target = this.target.get();
        ScreenUtils.bindTexture(BUTTONS);
        DyeColor color = this.color.get();
        float x = color == null ? 0 : (color.getId() + 1) * 8f / 136f;
        float y = target != null && this.isHovered() ? 0.5f : 0;
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, this.width, this.height, x, y, 8 / 136f, 0.5f);
    }

    @Override
    public void onPress(){
        if(this.visible)
            super.onPress();
    }

    @Override
    public Component getHoverText(){
        return this.visible ? Component.translatable("wormhole.portal.gui.target_color") : null;
    }

    @Override
    protected Component getNarrationMessage(){
        return this.visible ? Component.translatable("wormhole.portal.gui.target_color") : null;
    }
}






















