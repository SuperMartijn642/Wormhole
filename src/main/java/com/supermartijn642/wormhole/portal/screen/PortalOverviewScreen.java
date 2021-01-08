package com.supermartijn642.wormhole.portal.screen;

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
import net.minecraft.util.text.*;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

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
            IMessage packet = this.getFromPortalGroup(group -> group.isActive() ? new PortalDeactivatePacket(group) : new PortalActivatePacket(group), null);
            if(packet != null)
                Wormhole.channel.sendToServer(packet);
        }));
        Supplier<Integer> energy = () -> this.getPortalGroup().getStoredEnergy(), capacity = () -> this.getPortalGroup().getEnergyCapacity();
        this.addWidget(new EnergyBarWidget(8, 49, 30, 82, energy, capacity));
        // active target
        Supplier<Integer> activeTarget = () -> this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, 0);
        this.addWidget(new PortalTargetNameField(this, activeTarget, 20, 171));
        this.addWidget(new PortalTargetLabel(this, activeTarget, 85, 171, 100, 10, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false));
        this.addWidget(new PortalTargetEditColorButton(this, this.pos, 190, 171, activeTarget,
            () -> this.getFromPortalGroup(group -> {
                PortalTarget target = group.getTarget(activeTarget.get());
                return target == null ? null : target.color;
            }, null),
            () -> ClientProxy.openPortalOverviewScreen(this.pos)));
        this.addWidget(new WormholeButton(54, 185, 100, 10, "wormhole.portal.gui.select_target", () -> ClientProxy.openPortalTargetScreen(this.pos)));
    }

    @Override
    protected void render(int mouseX, int mouseY){
        this.activateButton.setTextKey(this.getFromPortalGroup(group -> group.isActive() ? "wormhole.portal.gui.deactivate" : "wormhole.portal.gui.activate", "wormhole.portal.gui.activate"));

        this.drawBackground(0, 0, this.sizeX(), this.sizeY());
        this.fontRenderer.drawString(this.title.getFormattedText(), 8, 7, 4210752);
        // info
        PortalStatus status = this.getFromPortalGroup(group -> {
            int energy = group.getStoredEnergy();
            return group.getActiveTarget() == null ? PortalStatus.NO_TARGET : energy == 0 ? PortalStatus.NO_ENERGY :
                group.isActive() && energy < group.getIdleEnergyCost() ? PortalStatus.LOW_ENERGY : PortalStatus.OK;
        }, PortalStatus.OK);
        this.fontRenderer.drawString(new TextComponentTranslation("wormhole.portal.gui.status").getFormattedText(), 190, 49, 4210752);
        this.drawStringRightAligned(status.getStatus(), 312, 49);
        this.fontRenderer.drawString(new TextComponentTranslation("wormhole.portal.gui.idle_cost").getFormattedText(), 190, 61, 4210752);
        this.drawStringRightAligned(new TextComponentString(EnergyFormat.formatEnergyPerTick(this.getFromPortalGroup(PortalGroup::getIdleEnergyCost, 0))), 312, 61);
        this.fontRenderer.drawString(new TextComponentTranslation("wormhole.portal.gui.teleport_cost").getFormattedText(), 190, 73, 4210752);
        this.drawStringRightAligned(new TextComponentString(this.getFromPortalGroup(PortalGroup::getActiveTarget, null) == null ? "--" : EnergyFormat.formatEnergy(this.getFromPortalGroup(PortalGroup::getTeleportEnergyCost, 0))), 312, 73);
        // target number
        int activeTarget = this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, 0);
        this.fontRenderer.drawString((activeTarget + 1) + ".", 8, 173, 4210752);

        PortalGroup group = this.getPortalGroup();
        if(group != null)
            PortalRendererHelper.drawPortal(group.shape, 44, 20, 140, 140);
    }

    private void drawStringRightAligned(ITextComponent textComponent, int x, int y){
        String s = textComponent.getFormattedText();
        int width = this.fontRenderer.getStringWidth(s);
        this.fontRenderer.drawString(s, x - width, y, 4210752);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){

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
            return new TextComponentString(this.status).setStyle(new Style().setColor(this.color));
        }
    }
}
