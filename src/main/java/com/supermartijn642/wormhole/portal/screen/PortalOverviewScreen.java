package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeClient;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalActivatePacket;
import com.supermartijn642.wormhole.portal.packets.PortalDeactivatePacket;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * Created 11/10/2020 by SuperMartijn642
 */
public class PortalOverviewScreen extends PortalGroupScreen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/portal_overview_screen.png");
    private static final ResourceLocation LOCATION_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/location_icon.png");
    private static final ResourceLocation ENERGY_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/lightning_icon.png");
    private static final ResourceLocation TELEPORT_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/teleport_icon.png");
    private static final ResourceLocation STAR_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/star_icon.png");
    private static final ResourceLocation DIMENSION_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/dimension_icon.png");
    private static final ResourceLocation CHECKMARK_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/checkmark_icon.png");
    private static final ResourceLocation CROSS_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/cross_icon.png");
    private static final ResourceLocation WARNING_ICON = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/warning_icon.png");
    private static final ResourceLocation SEPARATOR = ResourceLocation.fromNamespaceAndPath("wormhole", "textures/gui/select_target_screen/separator.png");
    private static final int WIDTH = 280, HEIGHT = 185;

    private WormholeColoredButton activateButton;

    public PortalOverviewScreen(BlockPos pos){
        super(WIDTH, HEIGHT, pos);
    }

    @Override
    protected Component getNarrationMessage(PortalGroup object){
        return TextComponents.translation("wormhole.portal.gui.title").get();
    }

    @Override
    protected void addWidgets(PortalGroup group){
        this.activateButton = this.addWidget(new WormholeColoredButton(45, 159, 60, 15, TextComponents.empty().get(), () -> Wormhole.CHANNEL.sendToServer(this.object.isActive() ? new PortalDeactivatePacket(this.object) : new PortalActivatePacket(this.object))));
        Supplier<Integer> energy = () -> this.object.getStoredEnergy(), capacity = () -> this.object.getEnergyCapacity();
        this.addWidget(new EnergyBarWidget(244, 55, 30, 82, energy, capacity));
        this.addWidget(new ButtonWidget(151, 159, 82, 13, TextComponents.translation("wormhole.portal.gui.change_target").get(), () -> WormholeClient.openPortalTargetScreen(this.pos)));
    }

    @Override
    protected void update(PortalGroup group){
        super.update(object);

        this.activateButton.setText(TextComponents.translation(group.isActive() ? "wormhole.portal.gui.deactivate" : "wormhole.portal.gui.activate").get());
        if(group.isActive())
            this.activateButton.setColorRed();
        else
            this.activateButton.setColorGreen();
    }

    @Override
    protected void renderBackground(WidgetRenderContext context, int mouseX, int mouseY, PortalGroup object){
        ScreenUtils.drawTexture(BACKGROUND, context.poseStack(), 0, 0, this.width(), this.height());
        super.renderBackground(context, mouseX, mouseY, object);
    }

    @Override
    protected void render(WidgetRenderContext context, int mouseX, int mouseY, PortalGroup group){
        super.render(context, mouseX, mouseY, group);

        ScreenUtils.drawCenteredString(context.poseStack(), TextComponents.translation("wormhole.portal.gui.title").get(), 72.5f, 3, Integer.MAX_VALUE);

        PortalTarget target = group.getActiveTarget();
        this.renderInfo(context.poseStack(), group.getStoredEnergy(), group.getIdleEnergyCost(), group.getTeleportEnergyCost(), target);
    }

    private void renderInfo(PoseStack poseStack, int storedEnergy, int idleCost, int teleportCost, PortalTarget target){
        PortalStatus status = target == null ? PortalStatus.NO_TARGET : storedEnergy == 0 ? PortalStatus.NO_ENERGY :
            storedEnergy < idleCost ? PortalStatus.NOT_ENOUGH_ENERGY : PortalStatus.OK;

        ScreenUtils.drawCenteredString(poseStack, TextComponents.translation("wormhole.portal.gui.information").get(), 192, 31, Integer.MAX_VALUE);

        ScreenUtils.drawTexture(SEPARATOR, poseStack, 154, 41, 77, 1);

        // status
        ScreenUtils.drawTexture(status.getIcon(), poseStack, 151, 47, 9, 9);
        ScreenUtils.drawString(poseStack, status.getStatus(), 162, 48, Integer.MAX_VALUE);
        // idle cost
        ScreenUtils.drawTexture(ENERGY_ICON, poseStack, 151, 59, 9, 9);
        ScreenUtils.drawString(poseStack, EnergyFormat.formatEnergyPerTick(idleCost), 162, 60, Integer.MAX_VALUE);
        // teleport cost
        ScreenUtils.drawTexture(TELEPORT_ICON, poseStack, 150, 70, 11, 11);
        ScreenUtils.drawString(poseStack, target == null ? "--" : EnergyFormat.formatEnergyWithUnit(teleportCost), 162, 72, Integer.MAX_VALUE);

        ScreenUtils.drawTexture(SEPARATOR, poseStack, 154, 85, 77, 1);

        // target
        ScreenUtils.drawTexture(STAR_ICON, poseStack, 151, 91, 9, 9);
        ScreenUtils.drawString(poseStack, target == null ? "--" : target.name, 162, 92, Integer.MAX_VALUE);
        if(target != null){
            // location
            ScreenUtils.drawTexture(LOCATION_ICON, poseStack, 151, 103, 9, 9);
            ScreenUtils.drawString(poseStack, "(" + target.x + ", " + target.y + ", " + target.z + ")", 162, 104, Integer.MAX_VALUE);
            // dimension
            Block block = null;
            if(target.dimension.equals(Level.OVERWORLD))
                block = Blocks.DIRT_PATH;
            else if(target.dimension.equals(Level.NETHER))
                block = Blocks.NETHERRACK;
            else if(target.dimension.equals(Level.END))
                block = Blocks.END_STONE;
            if(block == null)
                ScreenUtils.drawTexture(DIMENSION_ICON, poseStack, 151, 115, 9, 9);
            else
                ScreenBlockRenderer.drawBlock(poseStack, block, 155.5, 119.5, 5.5, 45, 40);
            ScreenUtils.drawString(poseStack, target.getDimensionDisplayName(), 162, 116, Integer.MAX_VALUE);
        }
    }

    @Override
    protected void renderForeground(WidgetRenderContext context, int mouseX, int mouseY, PortalGroup group){
        super.renderForeground(context, mouseX, mouseY, group);

        PortalRendererHelper.drawPortal(context.poseStack(), group.shape, this.x + 8, this.y + 19, 132, 132);
    }

    @Override
    protected void renderTooltips(WidgetRenderContext context, int mouseX, int mouseY, PortalGroup group){
        // status
        if(mouseX >= 150 && mouseX <= 161 && mouseY >= 46 && mouseY <= 57)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.status").get(), mouseX, mouseY);
            // idle cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 58 && mouseY <= 69)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.idle_cost").get(), mouseX, mouseY);
            // teleport cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 70 && mouseY <= 81)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.teleport_cost").get(), mouseX, mouseY);
            // target
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 90 && mouseY <= 101)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.target").get(), mouseX, mouseY);
            // location
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 102 && mouseY <= 113)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.target_location").get(), mouseX, mouseY);
            // dimension
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 114 && mouseY <= 125)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("wormhole.portal.gui.target_dimension").get(), mouseX, mouseY);

        super.renderTooltips(context, mouseX, mouseY, group);
    }

    private enum PortalStatus {
        OK("ok", ChatFormatting.GREEN, CHECKMARK_ICON),
        NOT_ENOUGH_ENERGY("not_enough_energy", ChatFormatting.RED, CROSS_ICON),
        NO_ENERGY("no_energy", ChatFormatting.RED, CROSS_ICON),
        NO_TARGET("no_target", ChatFormatting.GOLD, WARNING_ICON),
        NO_DIMENSIONAL_CORE("no_dimensional_core", ChatFormatting.RED, CROSS_ICON);

        private final String status;
        private final ChatFormatting color;
        private final ResourceLocation icon;

        PortalStatus(String status, ChatFormatting color, ResourceLocation icon){
            this.status = status;
            this.color = color;
            this.icon = icon;
        }

        public Component getStatus(){
            return TextComponents.translation("wormhole.portal.gui.status." + this.status).color(this.color).get();
        }

        public ResourceLocation getIcon(){
            return this.icon;
        }
    }
}
