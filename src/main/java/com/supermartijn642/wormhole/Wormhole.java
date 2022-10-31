package com.supermartijn642.wormhole;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.wormhole.data.*;
import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellBlockEntity;
import com.supermartijn642.wormhole.energycell.EnergyCellType;
import com.supermartijn642.wormhole.generator.CoalGeneratorBlock;
import com.supermartijn642.wormhole.generator.CoalGeneratorBlockEntity;
import com.supermartijn642.wormhole.generator.CoalGeneratorContainer;
import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.packets.*;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellBlockEntity;
import com.supermartijn642.wormhole.targetcell.TargetCellType;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceMovePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceNamePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = "@mod_id@", name = "@mod_name@", version = "@mod_version@", dependencies = "required-after:forge@@forge_dependency@;required-after:supermartijn642corelib@@core_library_dependency@;required-after:supermartijn642configlib@@config_library_dependency@")
public class Wormhole {

    /*
    TODO
    - item tooltips
    - dimensional core
    - generators
    - screen
    - redstone

    - improved textures
     */

    public static final PacketChannel CHANNEL = PacketChannel.create("wormhole");

    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_frame", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static BaseBlock portal_frame;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_x", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static PortalBlock portal_x;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_y", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static PortalBlock portal_y;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_z", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static PortalBlock portal_z;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_stabilizer", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static BaseBlock portal_stabilizer;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "basic_energy_cell", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EnergyCellBlock basic_energy_cell;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "advanced_energy_cell", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EnergyCellBlock advanced_energy_cell;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "creative_energy_cell", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static EnergyCellBlock creative_energy_cell;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "target_device", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static BaseItem target_device;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "advanced_target_device", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static BaseItem advanced_target_device;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "basic_target_cell", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static TargetCellBlock basic_target_cell;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "advanced_target_cell", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static TargetCellBlock advanced_target_cell;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "coal_generator", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static CoalGeneratorBlock coal_generator;

    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_frame_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<?> portal_frame_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<?> portal_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "stabilizer_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<?> stabilizer_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "basic_energy_cell_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<EnergyCellBlockEntity> basic_energy_cell_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "advanced_energy_cell_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<EnergyCellBlockEntity> advanced_energy_cell_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "creative_energy_cell_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<EnergyCellBlockEntity> creative_energy_cell_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "basic_target_cell_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<TargetCellBlockEntity> basic_target_cell_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "advanced_target_cell_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<TargetCellBlockEntity> advanced_target_cell_tile;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "coal_generator_tile", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<CoalGeneratorBlockEntity> coal_generator_tile;

    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "coal_generator_container", registry = RegistryEntryAcceptor.Registry.MENU_TYPES)
    public static BaseContainerType<CoalGeneratorContainer> coal_generator_container;

    public static final CreativeItemGroup ITEM_GROUP = CreativeItemGroup.create("wormhole", () -> advanced_target_device).sortAlphabetically();

    public Wormhole(){
        CHANNEL.registerMessage(TargetDeviceAddPacket.class, TargetDeviceAddPacket::new, true);
        CHANNEL.registerMessage(TargetDeviceMovePacket.class, TargetDeviceMovePacket::new, true);
        CHANNEL.registerMessage(TargetDeviceRemovePacket.class, TargetDeviceRemovePacket::new, true);
        CHANNEL.registerMessage(TargetDeviceNamePacket.class, TargetDeviceNamePacket::new, true);
        CHANNEL.registerMessage(PortalAddTargetPacket.class, PortalAddTargetPacket::new, true);
        CHANNEL.registerMessage(PortalClearTargetPacket.class, PortalClearTargetPacket::new, true);
        CHANNEL.registerMessage(PortalMoveTargetPacket.class, PortalMoveTargetPacket::new, true);
        CHANNEL.registerMessage(PortalNameTargetPacket.class, PortalNameTargetPacket::new, true);
        CHANNEL.registerMessage(PortalSelectTargetPacket.class, PortalSelectTargetPacket::new, true);
        CHANNEL.registerMessage(UpdateGroupPacket.class, UpdateGroupPacket::new, true);
        CHANNEL.registerMessage(UpdateGroupsPacket.class, UpdateGroupsPacket::new, true);
        CHANNEL.registerMessage(PortalColorTargetPacket.class, PortalColorTargetPacket::new, true);
        CHANNEL.registerMessage(PortalActivatePacket.class, PortalActivatePacket::new, true);
        CHANNEL.registerMessage(PortalDeactivatePacket.class, PortalDeactivatePacket::new, true);

        register();
        if(CommonUtils.getEnvironmentSide().isClient())
            WormholeClient.register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("wormhole");

        // Portal frame
        handler.registerBlock("portal_frame", () -> new PortalGroupBlock(() -> portal_frame_tile));
        handler.registerBlockEntityType("portal_frame_tile", () -> BaseBlockEntityType.create(() -> new PortalGroupBlockEntity(portal_frame_tile), portal_frame));
        handler.registerItem("portal_frame", () -> new BaseBlockItem(portal_frame, ItemProperties.create().group(ITEM_GROUP)));

        // Portal
        handler.registerBlock("portal_x", () -> new PortalBlock(EnumFacing.Axis.X));
        handler.registerBlock("portal_y", () -> new PortalBlock(EnumFacing.Axis.Y));
        handler.registerBlock("portal_z", () -> new PortalBlock(EnumFacing.Axis.Z));
        handler.registerBlockEntityType("portal_tile", () -> BaseBlockEntityType.create(PortalBlockEntity::new, portal_x, portal_y, portal_z));
        handler.registerItem("portal", () -> new BaseBlockItem(portal_x, ItemProperties.create()));

        // Portal stabilizer
        handler.registerBlock("portal_stabilizer", StabilizerBlock::new);
        handler.registerBlockEntityType("stabilizer_tile", () -> BaseBlockEntityType.create(StabilizerBlockEntity::new, portal_stabilizer));
        handler.registerItem("portal_stabilizer", () -> new BaseBlockItem(portal_stabilizer, ItemProperties.create().group(ITEM_GROUP)));

        // Energy cells
        for(EnergyCellType type : EnergyCellType.values()){
            handler.registerBlock(type.getRegistryName(), () -> new EnergyCellBlock(type));
            handler.registerBlockEntityType(type.getRegistryName() + "_tile", () -> BaseBlockEntityType.create(type::createTile, type.getBlock()));
            handler.registerItem(type.getRegistryName(), () -> new BaseBlockItem(type.getBlock(), ItemProperties.create().group(ITEM_GROUP)));
        }

        // Target cells
        for(TargetCellType type : TargetCellType.values()){
            handler.registerBlock(type.getRegistryName(), () -> new TargetCellBlock(type));
            handler.registerBlockEntityType(type.getRegistryName() + "_tile", () -> BaseBlockEntityType.create(type::createTile, type.getBlock()));
            handler.registerItem(type.getRegistryName(), () -> new BaseBlockItem(type.getBlock(), ItemProperties.create().group(ITEM_GROUP)));
        }

        // Coal generator
        handler.registerBlock("coal_generator", CoalGeneratorBlock::new);
        handler.registerBlockEntityType("coal_generator_tile", () -> BaseBlockEntityType.create(CoalGeneratorBlockEntity::new, coal_generator));
        handler.registerItem("coal_generator", () -> new BaseBlockItem(coal_generator, ItemProperties.create().group(ITEM_GROUP)));
        handler.registerMenuType("coal_generator_container", () -> BaseContainerType.create((container, data) -> data.writeBlockPos(container.getBlockEntityPos()), (player, data) -> new CoalGeneratorContainer(player, data.readBlockPos())));

        // Target devices
        handler.registerItem("target_device", () -> new TargetDeviceItem(WormholeConfig.basicDeviceTargetCount));
        handler.registerItem("advanced_target_device", () -> new TargetDeviceItem(WormholeConfig.advancedDeviceTargetCount));
    }

    private static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get("wormhole");
        handler.addGenerator(WormholeModelGenerator::new);
        handler.addGenerator(WormholeBlockStateGenerator::new);
        handler.addGenerator(WormholeLanguageGenerator::new);
        handler.addGenerator(WormholeLootTableGenerator::new);
        handler.addGenerator(WormholeRecipeGenerator::new);
        handler.addGenerator(WormholeTagGenerator::new);
    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent e){
        PortalGroupCapability.register();
    }

    @Mod.EventBusSubscriber
    public static class Events {

        @SubscribeEvent
        public static void handleMissingMappings(RegistryEvent.MissingMappings<Item> e){
            // Ignore warnings about the old portal items
            e.getMappings()
                .stream()
                .filter(mapping -> mapping.key.getResourceDomain().equals("wormhole") && (mapping.key.getResourcePath().equals("portal_x") || mapping.key.getResourcePath().equals("portal_y") || mapping.key.getResourcePath().equals("portal_z")))
                .forEach(mapping -> mapping.remap(Item.getItemFromBlock(portal_x)));
        }
    }
}
