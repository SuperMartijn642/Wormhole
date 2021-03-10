package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalActivatePacket;
import com.supermartijn642.wormhole.portal.packets.PortalDeactivatePacket;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.WormholeButton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Supplier;

/**
 * Created 11/10/2020 by SuperMartijn642
 */
public class PortalOverviewScreen extends PortalGroupScreen {

    private static final int WIDTH = 320, HEIGHT = 200;

    private WormholeButton activateButton;

    public PortalOverviewScreen(BlockPos pos){
        super("wormhole.portal.gui.title", pos);
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
        this.activateButton = this.addWidget(new WormholeButton(219, 117, 66, 14, "", () -> {
            Object packet = this.getFromPortalGroup(group -> group.isActive() ? new PortalDeactivatePacket(group) : new PortalActivatePacket(group), null);
            if(packet != null)
                Wormhole.CHANNEL.sendToServer(packet);
        }));
        Supplier<Integer> energy = () -> this.getPortalGroup().getStoredEnergy(), capacity = () -> this.getPortalGroup().getEnergyCapacity();
        this.addWidget(new EnergyBarWidget(8, 49, 30, 82, energy, capacity));
        // active target
        Supplier<Integer> activeTarget = () -> this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, 0);
        this.addWidget(new PortalTargetNameField(this, activeTarget, 20, 171));
        this.addWidget(new PortalTargetLabel(this, activeTarget, 84, 170, 102, 12, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false));
        this.addWidget(new PortalTargetEditColorButton(this, 190, 171, activeTarget,
            () -> this.getFromPortalGroup(group -> {
                PortalTarget target = group.getTarget(activeTarget.get());
                return target == null ? null : target.color;
            }, null),
            () -> ClientProxy.openPortalOverviewScreen(this.pos)));
        this.addWidget(new WormholeButton(54, 185, 100, 10, "wormhole.portal.gui.change_target", () -> ClientProxy.openPortalTargetScreen(this.pos)));
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        this.activateButton.setTextKey(this.getFromPortalGroup(group -> group.isActive() ? "wormhole.portal.gui.deactivate" : "wormhole.portal.gui.activate", "wormhole.portal.gui.activate"));

        ScreenUtils.drawScreenBackground(matrixStack, 0, 0, this.sizeX(), this.sizeY());
        this.font.func_243248_b(matrixStack, this.title, 8, 7, 4210752);
        // info
        PortalStatus status = this.getFromPortalGroup(group -> {
            int energy = group.getStoredEnergy();
            return group.getActiveTarget() == null ? PortalStatus.NO_TARGET : energy == 0 ? PortalStatus.NO_ENERGY :
                group.isActive() && energy < group.getIdleEnergyCost() ? PortalStatus.LOW_ENERGY : PortalStatus.OK;
        }, PortalStatus.OK);
        this.font.func_243248_b(matrixStack, new TranslationTextComponent("wormhole.portal.gui.status"), 190, 49, 4210752);
        this.drawStringRightAligned(matrixStack, status.getStatus(), 312, 49);
        this.font.func_243248_b(matrixStack, new TranslationTextComponent("wormhole.portal.gui.idle_cost"), 190, 61, 4210752);
        this.drawStringRightAligned(matrixStack, new StringTextComponent(EnergyFormat.formatEnergyPerTick(this.getFromPortalGroup(PortalGroup::getIdleEnergyCost, 0))), 312, 61);
        this.font.func_243248_b(matrixStack, new TranslationTextComponent("wormhole.portal.gui.teleport_cost"), 190, 73, 4210752);
        this.drawStringRightAligned(matrixStack, new StringTextComponent(this.getFromPortalGroup(PortalGroup::getActiveTarget, null) == null ? "--" : EnergyFormat.formatEnergy(this.getFromPortalGroup(PortalGroup::getTeleportEnergyCost, 0))), 312, 73);
        // target number
        int activeTarget = this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, 0);
        this.font.drawString(matrixStack, (activeTarget + 1) + ".", 8, 173, 4210752);

        PortalGroup group = this.getPortalGroup();
        if(group != null)
            PortalRendererHelper.drawPortal(group.shape, this.left() + 44, this.top() + 20, 140, 140);
    }

    private void drawStringRightAligned(MatrixStack matrixStack, ITextComponent textComponent, int x, int y){
        int width = this.font.getStringPropertyWidth(textComponent);
        this.font.func_243248_b(matrixStack, textComponent, x - width, y, 4210752);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){

    }

    private enum PortalStatus {
        OK("OK", TextFormatting.GREEN), // TODO translate
        LOW_ENERGY("LOW ENERGY", TextFormatting.GOLD),
        NO_ENERGY("NO ENERGY", TextFormatting.RED),
        NO_TARGET("NO TARGET", TextFormatting.GOLD),
        NO_DIMENSIONAL_CORE("NO DIMENSIONAL CORE", TextFormatting.RED);

        private String status;
        private TextFormatting color;

        PortalStatus(String status, TextFormatting color){
            this.status = status;
            this.color = color;
        }

        public ITextComponent getStatus(){
            return new StringTextComponent(this.status).mergeStyle(this.color);
        }
    }
}
