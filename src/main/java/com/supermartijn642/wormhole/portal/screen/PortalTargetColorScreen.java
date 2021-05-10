package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.screen.WormholeButton;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
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
        this.addWidget(new PortalTargetLabel(this, () -> this.targetIndex, 187, 19, 52, 12, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + EnumFacing.fromAngle(target.yaw).toString(), true));

        this.addWidget(new PortalTargetSelectColorButton(11, 38, this, null));
        this.addWidget(new PortalTargetSelectColorButton(49, 38, this, EnumDyeColor.WHITE));
        this.addWidget(new PortalTargetSelectColorButton(87, 38, this, EnumDyeColor.ORANGE));
        this.addWidget(new PortalTargetSelectColorButton(125, 38, this, EnumDyeColor.MAGENTA));
        this.addWidget(new PortalTargetSelectColorButton(163, 38, this, EnumDyeColor.LIGHT_BLUE));
        this.addWidget(new PortalTargetSelectColorButton(201, 38, this, EnumDyeColor.YELLOW));
        this.addWidget(new PortalTargetSelectColorButton(11, 76, this, EnumDyeColor.LIME));
        this.addWidget(new PortalTargetSelectColorButton(49, 76, this, EnumDyeColor.PINK));
        this.addWidget(new PortalTargetSelectColorButton(87, 76, this, EnumDyeColor.GRAY));
        this.addWidget(new PortalTargetSelectColorButton(125, 76, this, EnumDyeColor.SILVER));
        this.addWidget(new PortalTargetSelectColorButton(163, 76, this, EnumDyeColor.CYAN));
        this.addWidget(new PortalTargetSelectColorButton(201, 76, this, EnumDyeColor.PURPLE));
        this.addWidget(new PortalTargetSelectColorButton(11, 114, this, EnumDyeColor.BLUE));
        this.addWidget(new PortalTargetSelectColorButton(49, 114, this, EnumDyeColor.BROWN));
        this.addWidget(new PortalTargetSelectColorButton(87, 114, this, EnumDyeColor.GREEN));
        this.addWidget(new PortalTargetSelectColorButton(125, 114, this, EnumDyeColor.RED));
        this.addWidget(new PortalTargetSelectColorButton(163, 114, this, EnumDyeColor.BLACK));

        this.addWidget(new WormholeButton(124 - 30, 157, 60, 10, "wormhole.portal.color.gui.complete", this.returnScreen));
    }

    @Override
    protected void render(int mouseX, int mouseY, PortalGroup group){
        ScreenUtils.drawScreenBackground(0, 0, this.sizeX(), this.sizeY());
        ScreenUtils.drawString(this.title, 8, 7);
        // target number
        ScreenUtils.drawString((this.targetIndex + 1) + ".", 8, 22);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY, PortalGroup group){
    }
}
