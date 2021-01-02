package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalColorTargetPacket;
import com.supermartijn642.wormhole.screen.WormholeAbstractButton;
import net.minecraft.client.Minecraft;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Created 11/15/2020 by SuperMartijn642
 */
public class PortalTargetSelectColorButton extends WormholeAbstractButton {

    public static final ResourceLocation BUTTON_OUTLINE = new ResourceLocation("wormhole", "textures/gui/large_color_buttons.png");
    public static final ResourceLocation RANDOM_COLOR_PORTAL = new ResourceLocation("wormhole", "textures/gui/random_color_portal.png");

    private final DyeColor color;
    private final Supplier<DyeColor> targetColor;

    public PortalTargetSelectColorButton(int x, int y, PortalTargetColorScreen screen, DyeColor color){
        super(x, y, 36, 36, "color", () ->
            Wormhole.CHANNEL.sendToServer(new PortalColorTargetPacket(screen.getPortalGroup(), screen.targetIndex, color))
        );
        this.color = color;
        this.targetColor = () -> screen.getFromPortalGroup(group -> {
            PortalTarget target = group.getTarget(screen.targetIndex);
            return target == null ? null : target.color;
        }, null);
    }

    @Override
    protected void renderButton(MatrixStack matrixStack, int mouseX, int mouseY){
        Minecraft.getInstance().getTextureManager().bindTexture(BUTTON_OUTLINE);
        this.drawTexture(matrixStack, this.x, this.y, this.width, this.height, 0, this.targetColor.get() == this.color ? 2 / 3f : this.isHovered() ? 1 / 3f : 0, 1, 1 / 3f);
        Minecraft.getInstance().getTextureManager().bindTexture(this.color == null ? RANDOM_COLOR_PORTAL : new ResourceLocation("wormhole", "textures/portal/portal_" + this.color.getTranslationKey() + ".png"));
        this.drawTexture(matrixStack, this.x + 2, this.y + 2, this.width - 4, this.height - 4, 0, 0, 1, 16 / 512f);
    }
}
