package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.targetcell.TargetCellType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/**
 * Created 06/10/2022 by SuperMartijn642
 */
public class WormholeModelGenerator extends ModelGenerator {

    public WormholeModelGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        // Portal frame
        this.cubeAll("block/portal_frame", Identifier.fromNamespaceAndPath("wormhole", "portal_frame"));

        // Portal templates
        this.model("block/portals/portal_x")
            .parent("minecraft", "block/block")
            .texture("portal", "#all")
            .particleTexture("#all")
            .element(
                element -> element.shape(6, 0, 0, 10, 16, 16)
                    .face(Direction.EAST, face -> face.texture("portal"))
                    .face(Direction.WEST, face -> face.texture("portal"))
            );
        this.model("block/portals/portal_y")
            .parent("minecraft", "block/block")
            .texture("portal", "#all")
            .particleTexture("#all")
            .element(
                element -> element.shape(0, 6, 0, 16, 10, 16)
                    .face(Direction.UP, face -> face.texture("portal"))
                    .face(Direction.DOWN, face -> face.texture("portal"))
            );
        this.model("block/portals/portal_z")
            .parent("minecraft", "block/block")
            .texture("portal", "#all")
            .particleTexture("#all")
            .element(
                element ->
                    element.shape(0, 0, 6, 16, 16, 10)
                        .face(Direction.NORTH, face -> face.texture("portal"))
                        .face(Direction.SOUTH, face -> face.texture("portal"))
            );
        // Actual portals
        for(DyeColor color : DyeColor.values()){
            this.model("block/portals/portal_x_" + color.getName()).parent("block/portals/portal_x").texture("all", "portal/portal_" + color.getName());
            this.model("block/portals/portal_y_" + color.getName()).parent("block/portals/portal_y").texture("all", "portal/portal_" + color.getName());
            this.model("block/portals/portal_z_" + color.getName()).parent("block/portals/portal_z").texture("all", "portal/portal_" + color.getName());
        }

        // Portal stabilizer
        this.cubeAll("block/portal_stabilizer_off", Identifier.fromNamespaceAndPath("wormhole", "portal_stabilizer_off"));
        this.cubeAll("block/portal_stabilizer_on", Identifier.fromNamespaceAndPath("wormhole", "portal_stabilizer_on"));

        // Energy cells
        for(int i = 0; i < 16; i++){
            this.cubeAll("block/energy_cells/basic_energy_cell_" + i, Identifier.fromNamespaceAndPath("wormhole", "energy_cells/basic_energy_cell_" + i));
            this.cubeAll("block/energy_cells/advanced_energy_cell_" + i, Identifier.fromNamespaceAndPath("wormhole", "energy_cells/basic_energy_cell_" + i));
        }
        this.cubeAll("block/energy_cells/creative_energy_cell", Identifier.fromNamespaceAndPath("wormhole", "energy_cells/basic_energy_cell_15"));
        this.model("item/basic_energy_cell").parent("block/energy_cells/basic_energy_cell_0");
        this.model("item/advanced_energy_cell").parent("block/energy_cells/advanced_energy_cell_0");
        this.model("item/creative_energy_cell").parent("block/energy_cells/creative_energy_cell");

        // Target cells
        for(TargetCellType type : TargetCellType.values()){
            for(int i = 0; i <= type.getVisualCapacity(); i++)
                this.cubeAll("block/target_cells/" + type.getRegistryName() + "_" + i, Identifier.fromNamespaceAndPath("wormhole", "target_cells/" + type.getRegistryName() + "_" + i));
        }

        // Target devices
        this.itemGenerated("item/target_device", Identifier.fromNamespaceAndPath("wormhole", "target_device"));
        this.itemGenerated("item/advanced_target_device", Identifier.fromNamespaceAndPath("wormhole", "advanced_target_device"));
    }
}
