package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetEditColorButton;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetSelectColorButton;
import com.supermartijn642.wormhole.screen.ArrowButton;
import com.supermartijn642.wormhole.screen.EnergyBarWidget;
import com.supermartijn642.wormhole.screen.FlameProgressWidget;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.world.item.DyeColor;

/**
 * Created 20/01/2023 by SuperMartijn642
 */
public class WormholeAtlasSourceGenerator extends AtlasSourceGenerator {

    public WormholeAtlasSourceGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        this.blockAtlas().texturesFromModel("block/coal_generator_lit");

        this.guiAtlas()
            .texture(PortalOverviewScreen.BACKGROUND)
            .texture(PortalOverviewScreen.LOCATION_ICON)
            .texture(PortalOverviewScreen.ENERGY_ICON)
            .texture(PortalOverviewScreen.TELEPORT_ICON)
            .texture(PortalOverviewScreen.STAR_ICON)
            .texture(PortalOverviewScreen.DIMENSION_ICON)
            .texture(PortalOverviewScreen.CHECKMARK_ICON)
            .texture(PortalOverviewScreen.CROSS_ICON)
            .texture(PortalOverviewScreen.WARNING_ICON)
            .texture(PortalOverviewScreen.SEPARATOR)
            .texture(WormholeColoredButton.RED_BUTTONS)
            .texture(WormholeColoredButton.GREEN_BUTTONS)
            .texture(PortalTargetEditColorButton.BUTTONS)
            .texture(PortalTargetScreen.BACKGROUND)
            .texture(PortalTargetScreen.BACKGROUND_WITH_DEVICE)
            .texture(PortalTargetScreen.SELECT_HIGHLIGHT)
            .texture(PortalTargetScreen.SELECT_HIGHLIGHT_DEVICE)
            .texture(PortalTargetScreen.HOVER_HIGHLIGHT)
            .texture(PortalTargetScreen.HOVER_HIGHLIGHT_DEVICE)
            .texture(PortalTargetScreen.LOCATION_ICON)
            .texture(PortalTargetScreen.ENERGY_ICON)
            .texture(PortalTargetScreen.DIMENSION_ICON)
            .texture(PortalTargetScreen.DIRECTION_ICON)
            .texture(PortalTargetScreen.STAR_ICON)
            .texture(PortalTargetScreen.SEPARATOR)
            .texture(ArrowButton.BUTTONS)
            .texture(PortalTargetSelectColorButton.BUTTON_OUTLINE)
            .texture(PortalTargetSelectColorButton.RANDOM_COLOR_PORTAL)
            .texture(EnergyBarWidget.BARS)
            .texture(FlameProgressWidget.FLAME)
            .texture(TargetDeviceScreen.BACKGROUND)
            .texture(TargetDeviceScreen.SELECT_HIGHLIGHT)
            .texture(TargetDeviceScreen.HOVER_HIGHLIGHT)
            .texture(TargetDeviceScreen.LOCATION_ICON)
            .texture(TargetDeviceScreen.ENERGY_ICON)
            .texture(TargetDeviceScreen.DIMENSION_ICON)
            .texture(TargetDeviceScreen.DIRECTION_ICON)
            .texture(TargetDeviceScreen.SEPARATOR);
        for(DyeColor color : DyeColor.values())
            this.guiAtlas().texture("portal/portal_" + color.getName());
    }
}
