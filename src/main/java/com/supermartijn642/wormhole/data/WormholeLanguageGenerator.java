package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.Wormhole;

/**
 * Created 10/10/2022 by SuperMartijn642
 */
public class WormholeLanguageGenerator extends LanguageGenerator {

    public WormholeLanguageGenerator(ResourceCache cache){
        super("wormhole", cache, "en_us");
    }

    @Override
    public void generate(){
        // Portal frame
        this.block(Wormhole.portal_frame, "Portal Frame");

        // Portal block
        this.block(Wormhole.portal, "Portal");

        // Portal stabilizer
        this.block(Wormhole.portal_stabilizer, "Portal Stabilizer");
        this.translation("wormhole.portal_stabilizer.error", "Invalid portal construction!");
        this.translation("wormhole.portal_stabilizer.success", "Wormhole stabilized!");
        this.translation("wormhole.portal_stabilizer.info", "A controller for portals, at least one is required per portal");
        this.translation("wormhole.portal_stabilizer.info.targets", "%1$d / %2$d targets stored");

        // Energy cells
        this.block(Wormhole.basic_energy_cell, "Basic Energy Cell");
        this.block(Wormhole.advanced_energy_cell, "Advanced Energy Cell");
        this.block(Wormhole.creative_energy_cell, "Creative Energy Cell");
        this.translation("wormhole.energy_cell.info", "Can store energy for use by portals");

        // Target cells
        this.block(Wormhole.basic_target_cell, "Basic Target Cell");
        this.block(Wormhole.advanced_target_cell, "Advanced Target Cell");
        this.translation("wormhole.target_cell.info", "Can store targets for use by portals");

        // Coal generator
        this.block(Wormhole.coal_generator, "Coal Generator");
        this.translation("wormhole.coal_generator.info", "Burns coal to charge blocks in a %1$dx%1$dx%1$d area, generates %2$s");

        // Target devices
        this.item(Wormhole.target_device, "Target Definition Device");
        this.item(Wormhole.advanced_target_device, "Advanced Target Definition Device");
        this.translation("wormhole.target_device.gui.title", "Device Targets");
        this.translation("wormhole.target_device.gui.current_location", "Current Location");
        this.translation("wormhole.target_device.gui.target_name", "Target Name");
        this.translation("wormhole.target_device.gui.coords", "Coordinates (x,y,z)");
        this.translation("wormhole.target_device.gui.facing", "Facing");
        this.translation("wormhole.target_device.info", "Can define targets for portals");
        this.translation("wormhole.target_device.info.targets", "%1$d / %2$d targets stored");

        // Creative tab
        this.itemGroup(Wormhole.ITEM_GROUP, "Wormhole");

        // Portal guis
        this.translation("wormhole.portal.targets.gui.title", "Portal Targets");
        this.translation("wormhole.portal.gui.title", "Portal Overview");
        this.translation("wormhole.portal.gui.target_color", "Color (Click to edit)");
        this.translation("wormhole.portal.color.gui.title", "Target Color");
        this.translation("wormhole.portal.gui.change_target", "Change Target");
        this.translation("wormhole.portal.color.gui.complete", "Done");
        this.translation("wormhole.portal.gui.activate", "Activate");
        this.translation("wormhole.portal.gui.deactivate", "Deactivate");
        this.translation("wormhole.portal.gui.status", "Portal Status");
        this.translation("wormhole.portal.gui.idle_cost", "Idle Energy Cost");
        this.translation("wormhole.portal.gui.teleport_cost", "Teleport Energy Cost");
        this.translation("wormhole.portal.gui.target", "Selected Target");
        this.translation("wormhole.portal.gui.target_location", "Target Location");
        this.translation("wormhole.portal.gui.target_dimension", "Target Dimension");
        this.translation("wormhole.portal.gui.information", "Information");
        this.translation("wormhole.portal.gui.status.ok", "OK");
        this.translation("wormhole.portal.gui.status.not_enough_energy", "NOT ENOUGH ENERGY");
        this.translation("wormhole.portal.gui.status.no_energy", "NO ENERGY");
        this.translation("wormhole.portal.gui.status.no_target", "NO TARGET");
        this.translation("wormhole.portal.gui.status.no_dimensional_core", "NO DIMENSIONAL CORE");
        this.translation("wormhole.portal.targets.gui.return", "Back");
        this.translation("wormhole.portal.targets.gui.select", "Select");
        this.translation("wormhole.portal.targets.gui.remove", "Remove");
        this.translation("wormhole.portal.targets.gui.add", "Add");

        // Generic
        this.translation("wormhole.gui.arrow_button.up", "Move Up");
        this.translation("wormhole.gui.arrow_button.down", "Move Down");
        this.translation("wormhole.direction.north", "North");
        this.translation("wormhole.direction.east", "East");
        this.translation("wormhole.direction.south", "South");
        this.translation("wormhole.direction.west", "West");
        this.translation("wormhole.color.white", "White");
        this.translation("wormhole.color.orange", "Orange");
        this.translation("wormhole.color.magenta", "Magenta");
        this.translation("wormhole.color.light_blue", "Light Blue");
        this.translation("wormhole.color.yellow", "Yellow");
        this.translation("wormhole.color.lime", "Lime");
        this.translation("wormhole.color.pink", "Pink");
        this.translation("wormhole.color.gray", "Gray");
        this.translation("wormhole.color.light_gray", "Light Gray");
        this.translation("wormhole.color.cyan", "Cyan");
        this.translation("wormhole.color.purple", "Purple");
        this.translation("wormhole.color.blue", "Blue");
        this.translation("wormhole.color.brown", "Brown");
        this.translation("wormhole.color.green", "Green");
        this.translation("wormhole.color.red", "Red");
        this.translation("wormhole.color.black", "Black");
        this.translation("wormhole.color.random", "Random");
        this.translation("wormhole.target.location", "Location");
        this.translation("wormhole.target.dimension", "Dimension");
        this.translation("wormhole.target.direction", "Direction");
        this.translation("wormhole.target.teleport_cost", "Teleport Cost");
    }
}
