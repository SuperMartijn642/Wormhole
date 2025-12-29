package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalColorTargetPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalTargetSelectColorButton extends AbstractButtonWidget {

    public static final Identifier BUTTON_OUTLINE = Identifier.fromNamespaceAndPath("wormhole", "gui/large_color_buttons");
    public static final Identifier RANDOM_COLOR_PORTAL = Identifier.fromNamespaceAndPath("wormhole", "gui/random_color_portal");

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
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitSprite(BUTTON_OUTLINE, this.x, this.y, this.width, this.height, p -> p.uv(0, this.targetColor.get() == this.color ? 2 / 3f : this.isFocused() ? 1 / 3f : 0, 1, 1 / 3f));
        Identifier texture = this.color == null ? RANDOM_COLOR_PORTAL : Identifier.fromNamespaceAndPath("wormhole", "textures/portal/portal_" + this.color.getName() + ".png");
        graphics.submitSprite(texture, this.x + 2, this.y + 2, this.width - 4, this.height - 4, p -> p.uv(0, 0, 1, 16 / 512f));
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("wormhole.color." + (this.color == null ? "random" : this.color.getName())).get();
    }
}
