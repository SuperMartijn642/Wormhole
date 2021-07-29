package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
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
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * Created 11/10/2020 by SuperMartijn642
 */
public class PortalOverviewScreen extends PortalGroupScreen {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("wormhole", "textures/gui/portal_overview_screen.png");
    private static final ResourceLocation LOCATION_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/location_icon.png");
    private static final ResourceLocation ENERGY_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/lightning_icon.png");
    private static final ResourceLocation TELEPORT_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/teleport_icon.png");
    private static final ResourceLocation STAR_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/star_icon.png");
    private static final ResourceLocation DIMENSION_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/dimension_icon.png");
    private static final ResourceLocation CHECKMARK_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/checkmark_icon.png");
    private static final ResourceLocation CROSS_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/cross_icon.png");
    private static final ResourceLocation WARNING_ICON = new ResourceLocation("wormhole", "textures/gui/select_target_screen/warning_icon.png");
    private static final ResourceLocation SEPARATOR = new ResourceLocation("wormhole", "textures/gui/select_target_screen/separator.png");
    private static final int WIDTH = 280, HEIGHT = 185;

    private WormholeColoredButton activateButton;

    public PortalOverviewScreen(BlockPos pos){
        super("wormhole.portal.gui.title", pos);
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
        this.activateButton = this.addWidget(new WormholeColoredButton(45, 159, 60, 15, "", () -> {
            PortalGroup group2 = this.getObject();
            if(group2 != null)
                Wormhole.CHANNEL.sendToServer(group2.isActive() ? new PortalDeactivatePacket(group2) : new PortalActivatePacket(group2));
        }));
        Supplier<Integer> energy = () -> this.getFromPortalGroup(PortalGroup::getStoredEnergy, 0), capacity = () -> this.getFromPortalGroup(PortalGroup::getEnergyCapacity, 0);
        this.addWidget(new EnergyBarWidget(244, 55, 30, 82, energy, capacity));
        this.addWidget(new WormholeButton(151, 159, 82, 13, "wormhole.portal.gui.change_target", () -> ClientProxy.openPortalTargetScreen(this.pos)));
    }

    @Override
    protected void render(PoseStack matrixStack, int mouseX, int mouseY, PortalGroup group){
        this.activateButton.setTextKey(group.isActive() ? "wormhole.portal.gui.deactivate" : "wormhole.portal.gui.activate");
        if(group.isActive())
            this.activateButton.setColorRed();
        else
            this.activateButton.setColorGreen();

        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(matrixStack, 0, 0, this.sizeX(), this.sizeY());
        ScreenUtils.drawCenteredString(matrixStack, this.title, 72.5f, 3, Integer.MAX_VALUE);

        PortalTarget target = group.getActiveTarget();
        this.renderInfo(matrixStack, group.getStoredEnergy(), group.getIdleEnergyCost(), group.getTeleportEnergyCost(), target);

        PortalRendererHelper.drawPortal(group.shape, this.left() + 8, this.top() + 19, 132, 132);
    }

    private void renderInfo(PoseStack matrixStack, int storedEnergy, int idleCost, int teleportCost, PortalTarget target){
        PortalStatus status = target == null ? PortalStatus.NO_TARGET : storedEnergy == 0 ? PortalStatus.NO_ENERGY :
            storedEnergy < idleCost ? PortalStatus.NOT_ENOUGH_ENERGY : PortalStatus.OK;

        ScreenUtils.drawCenteredString(matrixStack, new TranslatableComponent("wormhole.portal.gui.information"), 192, 31, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(matrixStack, 154, 41, 77, 1);

        // status
        ScreenUtils.bindTexture(status.getIcon());
        ScreenUtils.drawTexture(matrixStack, 151, 47, 9, 9);
        ScreenUtils.drawString(matrixStack, status.getStatus(), 162, 48, Integer.MAX_VALUE);
        // idle cost
        ScreenUtils.bindTexture(ENERGY_ICON);
        ScreenUtils.drawTexture(matrixStack, 151, 59, 9, 9);
        ScreenUtils.drawString(matrixStack, EnergyFormat.formatEnergyPerTick(idleCost), 162, 60, Integer.MAX_VALUE);
        // teleport cost
        ScreenUtils.bindTexture(TELEPORT_ICON);
        ScreenUtils.drawTexture(matrixStack, 150, 70, 11, 11);
        ScreenUtils.drawString(matrixStack, target == null ? "--" : EnergyFormat.formatEnergy(teleportCost), 162, 72, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(matrixStack, 154, 85, 77, 1);

        // target
        ScreenUtils.bindTexture(STAR_ICON);
        ScreenUtils.drawTexture(matrixStack, 151, 91, 9, 9);
        ScreenUtils.drawString(matrixStack, target == null ? "--" : target.name, 162, 92, Integer.MAX_VALUE);
        if(target != null){
            // location
            ScreenUtils.bindTexture(LOCATION_ICON);
            ScreenUtils.drawTexture(matrixStack, 151, 103, 9, 9);
            ScreenUtils.drawString(matrixStack, this.font, "(" + target.x + ", " + target.y + ", " + target.z + ")", 162, 104, Integer.MAX_VALUE);
            // dimension
            Block block = null;
            if(target.dimension.equals(Level.OVERWORLD.location().toString()))
                block = Blocks.DIRT_PATH;
            else if(target.dimension.equals(Level.NETHER.location().toString()))
                block = Blocks.NETHERRACK;
            else if(target.dimension.equals(Level.END.location().toString()))
                block = Blocks.END_STONE;
            if(block == null){
                ScreenUtils.bindTexture(DIMENSION_ICON);
                ScreenUtils.drawTexture(matrixStack, 151, 115, 9, 9);
            }else{
                ScreenBlockRenderer.drawBlock(block, this.left() + 155.5, this.top() + 119.5, 5.5, 45, 40);
            }
            ScreenUtils.drawString(matrixStack, this.font, target.getDimensionDisplayName(), 162, 116, Integer.MAX_VALUE);
        }
    }

    @Override
    protected void renderTooltips(PoseStack matrixStack, int mouseX, int mouseY, PortalGroup group){
        // status
        if(mouseX >= 150 && mouseX <= 161 && mouseY >= 46 && mouseY <= 57)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.status"), mouseX, mouseY);
            // idle cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 58 && mouseY <= 69)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.idle_cost"), mouseX, mouseY);
            // teleport cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 70 && mouseY <= 81)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.teleport_cost"), mouseX, mouseY);
            // target
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 90 && mouseY <= 101)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.target"), mouseX, mouseY);
            // location
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 102 && mouseY <= 113)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.target_location"), mouseX, mouseY);
            // dimension
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 114 && mouseY <= 125)
            this.renderTooltip(matrixStack, new TranslatableComponent("wormhole.portal.gui.target_dimension"), mouseX, mouseY);
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
            return new TranslatableComponent("wormhole.portal.gui.status." + this.status).withStyle(this.color);
        }

        public ResourceLocation getIcon(){
            return this.icon;
        }
    }
}
