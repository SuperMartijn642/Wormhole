package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.wormhole.screen.WormholeButton;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

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
    protected float sizeX(){
        return WIDTH;
    }

    @Override
    protected float sizeY(){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(){
        this.addWidget(new PortalTargetNameField(this, () -> this.targetIndex, 20, 20));
        this.addWidget(new PortalTargetLabel(this, () -> this.targetIndex, 85, 20, 100, 10, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false));
        this.addWidget(new PortalTargetLabel(this, () -> this.targetIndex, 188, 20, 50, 10, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + Direction.fromAngle(target.yaw).toString(), true));

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
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        this.drawBackground(matrixStack, 0, 0, this.sizeX(), this.sizeY());
        this.font.func_243248_b(matrixStack, this.title, 8, 7, 4210752);
        // target number
        this.font.drawString(matrixStack, (this.targetIndex + 1) + ".", 8, 22, 4210752);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){

    }
}
