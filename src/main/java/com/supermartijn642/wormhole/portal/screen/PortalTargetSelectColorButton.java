package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalColorTargetPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalTargetSelectColorButton extends AbstractButtonWidget {

    public static final ResourceLocation BUTTON_OUTLINE = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/large_color_buttons.png");
    public static final ResourceLocation RANDOM_COLOR_PORTAL = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/random_color_portal.png");

    private final DyeColor color;
    private final Supplier<DyeColor> targetColor;

    public PortalTargetSelectColorButton(int x, int y, PortalTargetColorScreen screen, DyeColor color){
        super(x, y, 36, 36, () -> Wormhole.CHANNEL.sendToServer(new PortalColorTargetPacket(screen.getPortalGroup(), screen.targetIndex, color)));
        this.color = color;
        this.targetColor = () -> {
            PortalTarget target = screen.getPortalGroup().getTarget(screen.targetIndex);
            return target == null ? null : target.color;
        };
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        ScreenUtils.drawTexture(BUTTON_OUTLINE, context.poseStack(), this.x, this.y, this.width, this.height, 0, this.targetColor.get() == this.color ? 2 / 3f : this.isFocused() ? 1 / 3f : 0, 1, 1 / 3f);
        ResourceLocation texture = this.color == null ? RANDOM_COLOR_PORTAL : ResourceLocation.fromNamespaceAndPath("wormhole", "textures/portal/portal_" + this.color.getName() + ".png");
        ScreenUtils.drawTexture(texture, context.poseStack(), this.x + 2, this.y + 2, this.width - 4, this.height - 4, 0, 0, 1, 16 / 512f);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("wormhole.color." + (this.color == null ? "random" : this.color.getName())).get();
    }
}
