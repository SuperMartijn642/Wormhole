package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * Created 11/10/2020 by SuperMartijn642
 */
public class PortalOverviewScreen extends PortalGroupScreen {

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath("wormhole", "gui/portal_overview_screen");
    public static final Identifier LOCATION_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/location_icon");
    public static final Identifier ENERGY_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/lightning_icon");
    public static final Identifier TELEPORT_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/teleport_icon");
    public static final Identifier STAR_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/star_icon");
    public static final Identifier DIMENSION_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/dimension_icon");
    public static final Identifier CHECKMARK_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/checkmark_icon");
    public static final Identifier CROSS_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/cross_icon");
    public static final Identifier WARNING_ICON = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/warning_icon");
    public static final Identifier SEPARATOR = Identifier.fromNamespaceAndPath("wormhole", "gui/select_target_screen/separator");
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
    protected void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, PortalGroup object){
        graphics.submitSprite(BACKGROUND, 0, 0, this.width(), this.height());
        super.renderBackground(context, graphics, mouseX, mouseY, object);
    }

    @Override
    protected void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, PortalGroup group){
        super.render(context, graphics, mouseX, mouseY, group);

        graphics.submitText(TextComponents.translation("wormhole.portal.gui.title").get(), 72.5f, 3, p -> p.color(Integer.MAX_VALUE).centerHorizontally());

        PortalTarget target = group.getActiveTarget();
        this.renderInfo(graphics, group.getStoredEnergy(), group.getIdleEnergyCost(), group.getTeleportEnergyCost(), target);
    }

    private void renderInfo(GuiGraphicsHelper graphics, int storedEnergy, int idleCost, int teleportCost, PortalTarget target){
        PortalStatus status = target == null ? PortalStatus.NO_TARGET : storedEnergy == 0 ? PortalStatus.NO_ENERGY :
            storedEnergy < idleCost ? PortalStatus.NOT_ENOUGH_ENERGY : PortalStatus.OK;

        graphics.submitText(TextComponents.translation("wormhole.portal.gui.information").get(), 192, 31, p -> p.color(Integer.MAX_VALUE).centerHorizontally());

        graphics.submitSprite(SEPARATOR, 154, 41, 77, 1);

        // status
        graphics.submitSprite(status.getIcon(), 151, 47, 9, 9);
        graphics.submitText(status.getStatus(), 162, 48, p -> p.color(Integer.MAX_VALUE));
        // idle cost
        graphics.submitSprite(ENERGY_ICON, 151, 59, 9, 9);
        graphics.submitText(EnergyFormat.formatEnergyPerTick(idleCost), 162, 60, p -> p.color(Integer.MAX_VALUE));
        // teleport cost
        graphics.submitSprite(TELEPORT_ICON, 150, 70, 11, 11);
        graphics.submitText(target == null ? "--" : EnergyFormat.formatEnergyWithUnit(teleportCost), 162, 72, p -> p.color(Integer.MAX_VALUE));

        graphics.submitSprite(SEPARATOR, 154, 85, 77, 1);

        // target
        graphics.submitSprite(STAR_ICON, 151, 91, 9, 9);
        graphics.submitText(target == null ? "--" : target.name, 162, 92, p -> p.color(Integer.MAX_VALUE));
        if(target != null){
            // location
            graphics.submitSprite(LOCATION_ICON, 151, 103, 9, 9);
            graphics.submitText("(" + target.x + ", " + target.y + ", " + target.z + ")", 162, 104, p -> p.color(Integer.MAX_VALUE));
            // dimension
            Block block;
            if(target.dimension.equals(Level.OVERWORLD))
                block = Blocks.DIRT_PATH;
            else if(target.dimension.equals(Level.NETHER))
                block = Blocks.NETHERRACK;
            else if(target.dimension.equals(Level.END))
                block = Blocks.END_STONE;
            else
                block = null;
            if(block == null)
                graphics.submitSprite(DIMENSION_ICON, 151, 115, 9, 9);
            else
                graphics.submitCustomRendering(
                    149, 113, 13, 13,
                    poseStack -> ScreenBlockRenderer.drawBlock(poseStack, block, 6.5, 6.5, 5.5, 45, 40)
                );
            graphics.submitText(target.getDimensionDisplayName(), 162, 116, p -> p.color(Integer.MAX_VALUE));
        }
    }

    @Override
    protected void renderForeground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, PortalGroup group){
        super.renderForeground(context, graphics, mouseX, mouseY, group);

        graphics.submitCustomRendering(
            this.x + 8, this.y + 19, 132, 132,
            poseStack -> PortalRendererHelper.drawPortal(poseStack, group.shape, 0, 0, 132, 132)
        );
    }

    @Override
    protected void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, PortalGroup group){
        // status
        if(mouseX >= 150 && mouseX <= 161 && mouseY >= 46 && mouseY <= 57)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.status").get()), mouseX, mouseY);
            // idle cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 58 && mouseY <= 69)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.idle_cost").get()), mouseX, mouseY);
            // teleport cost
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 70 && mouseY <= 81)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.teleport_cost").get()), mouseX, mouseY);
            // target
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 90 && mouseY <= 101)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.target").get()), mouseX, mouseY);
            // location
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 102 && mouseY <= 113)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.target_location").get()), mouseX, mouseY);
            // dimension
        else if(mouseX >= 150 && mouseX <= 161 && mouseY >= 114 && mouseY <= 125)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.portal.gui.target_dimension").get()), mouseX, mouseY);

        super.renderTooltips(context, graphics, mouseX, mouseY, group);
    }

    private enum PortalStatus {
        OK("ok", ChatFormatting.GREEN, CHECKMARK_ICON),
        NOT_ENOUGH_ENERGY("not_enough_energy", ChatFormatting.RED, CROSS_ICON),
        NO_ENERGY("no_energy", ChatFormatting.RED, CROSS_ICON),
        NO_TARGET("no_target", ChatFormatting.GOLD, WARNING_ICON),
        NO_DIMENSIONAL_CORE("no_dimensional_core", ChatFormatting.RED, CROSS_ICON);

        private final String status;
        private final ChatFormatting color;
        private final Identifier icon;

        PortalStatus(String status, ChatFormatting color, Identifier icon){
            this.status = status;
            this.color = color;
            this.icon = icon;
        }

        public Component getStatus(){
            return TextComponents.translation("wormhole.portal.gui.status." + this.status).color(this.color).get();
        }

        public Identifier getIcon(){
            return this.icon;
        }
    }
}
