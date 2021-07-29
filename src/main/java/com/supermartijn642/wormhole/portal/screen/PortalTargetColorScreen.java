package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.screen.WormholeButton;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

/**
 * Created 11/12/2020 by SuperMartijn642
 */
public class PortalTargetColorScreen extends PortalGroupScreen {

    private static final int WIDTH = 248, HEIGHT = 173;

    public final int targetIndex;
    private final Runnable returnScreen;

    public PortalTargetColorScreen(BlockPos pos, int targetIndex, Runnable returnScreen){
        super("wormhole.portal.color.gui.title", pos);
        this.targetIndex = targetIndex;
        this.returnScreen = returnScreen;
    }

    @Override
    protected float sizeX(PortalGroup group){
        return WIDTH;
    }

    @Override
    protected float sizeY(PortalGroup group){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(PortalGroup group){
        this.addWidget(new PortalTargetNameField(this, () -> this.targetIndex, 20, 20));
        this.addWidget(new PortalTargetLabel(this, () -> this.targetIndex, 84, 19, 102, 12, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false));
        this.addWidget(new PortalTargetLabel(this, () -> this.targetIndex, 187, 19, 52, 12, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + Direction.fromYRot(target.yaw).toString(), true));

        this.addWidget(new PortalTargetSelectColorButton(11, 38, this, null));
        this.addWidget(new PortalTargetSelectColorButton(49, 38, this, DyeColor.WHITE));
        this.addWidget(new PortalTargetSelectColorButton(87, 38, this, DyeColor.ORANGE));
        this.addWidget(new PortalTargetSelectColorButton(125, 38, this, DyeColor.MAGENTA));
        this.addWidget(new PortalTargetSelectColorButton(163, 38, this, DyeColor.LIGHT_BLUE));
        this.addWidget(new PortalTargetSelectColorButton(201, 38, this, DyeColor.YELLOW));
        this.addWidget(new PortalTargetSelectColorButton(11, 76, this, DyeColor.LIME));
        this.addWidget(new PortalTargetSelectColorButton(49, 76, this, DyeColor.PINK));
        this.addWidget(new PortalTargetSelectColorButton(87, 76, this, DyeColor.GRAY));
        this.addWidget(new PortalTargetSelectColorButton(125, 76, this, DyeColor.LIGHT_GRAY));
        this.addWidget(new PortalTargetSelectColorButton(163, 76, this, DyeColor.CYAN));
        this.addWidget(new PortalTargetSelectColorButton(201, 76, this, DyeColor.PURPLE));
        this.addWidget(new PortalTargetSelectColorButton(11, 114, this, DyeColor.BLUE));
        this.addWidget(new PortalTargetSelectColorButton(49, 114, this, DyeColor.BROWN));
        this.addWidget(new PortalTargetSelectColorButton(87, 114, this, DyeColor.GREEN));
        this.addWidget(new PortalTargetSelectColorButton(125, 114, this, DyeColor.RED));
        this.addWidget(new PortalTargetSelectColorButton(163, 114, this, DyeColor.BLACK));

        this.addWidget(new WormholeButton(124 - 30, 157, 60, 10, "wormhole.portal.color.gui.complete", this.returnScreen));
    }

    @Override
    protected void render(PoseStack matrixStack, int mouseX, int mouseY, PortalGroup group){
        ScreenUtils.drawScreenBackground(matrixStack, 0, 0, this.sizeX(), this.sizeY());
        ScreenUtils.drawString(matrixStack, this.title, 8, 7);
        // target number
        ScreenUtils.drawString(matrixStack, (this.targetIndex + 1) + ".", 8, 22);
    }

    @Override
    protected void renderTooltips(PoseStack matrixStack, int mouseX, int mouseY, PortalGroup group){
    }
}
