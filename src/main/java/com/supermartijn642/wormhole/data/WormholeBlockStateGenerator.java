package com.supermartijn642.wormhole.data;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.wormhole.PortalBlock;
import com.supermartijn642.wormhole.StabilizerBlock;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.generator.CoalGeneratorBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellType;
import net.minecraft.item.EnumDyeColor;

/**
 * Created 06/10/2022 by SuperMartijn642
 */
public class WormholeBlockStateGenerator extends BlockStateGenerator {

    public WormholeBlockStateGenerator(ResourceCache cache){
        super("wormhole", cache);
    }

    @Override
    public void generate(){
        // Portal frame
        this.blockState(Wormhole.portal_frame).emptyVariant(variant -> variant.model("block/portal_frame"));

        // Portals
        this.blockState(Wormhole.portal_x).variantsForAll((state, variant) -> {
            EnumDyeColor color = state.get(PortalBlock.COLOR_PROPERTY);
            variant.model("block/portals/portal_x_" + color.getName());
        });
        this.blockState(Wormhole.portal_y).variantsForAll((state, variant) -> {
            EnumDyeColor color = state.get(PortalBlock.COLOR_PROPERTY);
            variant.model("block/portals/portal_y_" + color.getName());
        });
        this.blockState(Wormhole.portal_z).variantsForAll((state, variant) -> {
            EnumDyeColor color = state.get(PortalBlock.COLOR_PROPERTY);
            variant.model("block/portals/portal_z_" + color.getName());
        });

        // Portal stabilizer
        this.blockState(Wormhole.portal_stabilizer).variantsForProperty(StabilizerBlock.ON_PROPERTY, (state, variant) -> variant.model("block/portal_stabilizer_" + (state.get(StabilizerBlock.ON_PROPERTY) ? "on" : "off")));

        // Energy cells
        this.blockState(Wormhole.basic_energy_cell).variantsForProperty(EnergyCellBlock.ENERGY_LEVEL, (state, variant) -> variant.model("block/energy_cells/basic_energy_cell_" + state.get(EnergyCellBlock.ENERGY_LEVEL)));
        this.blockState(Wormhole.advanced_energy_cell).variantsForProperty(EnergyCellBlock.ENERGY_LEVEL, (state, variant) -> variant.model("block/energy_cells/advanced_energy_cell_" + state.get(EnergyCellBlock.ENERGY_LEVEL)));
        this.blockState(Wormhole.creative_energy_cell).variantsForAll((state, variant) -> variant.model("block/energy_cells/creative_energy_cell"));

        // Target cells
        for(TargetCellType type : TargetCellType.values())
            this.blockState(type.getBlock()).variantsForProperty(TargetCellBlock.VISUAL_TARGETS, (state, variant) -> variant.model("block/target_cells/" + type.getRegistryName() + "_" + Math.min(type.getVisualCapacity(), state.get(TargetCellBlock.VISUAL_TARGETS))));

        // Coal generator
        this.blockState(Wormhole.coal_generator).variantsForAll((state, variant) -> {
            boolean lit = state.get(CoalGeneratorBlock.LIT);
            int rotation = (int)-state.get(CoalGeneratorBlock.FACING).getHorizontalAngle() % 360;
            variant.model(lit ? "block/coal_generator_lit" : "block/coal_generator", 0, rotation);
        });
    }
}
