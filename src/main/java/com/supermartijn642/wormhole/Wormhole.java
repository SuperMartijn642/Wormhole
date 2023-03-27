package com.supermartijn642.wormhole;

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
import net.fabricmc.api.ModInitializer;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
public class Wormhole implements ModInitializer {

    public static final PacketChannel CHANNEL = PacketChannel.create("wormhole");

    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal_frame", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static BaseBlock portal_frame;
    @RegistryEntryAcceptor(namespace = "wormhole", identifier = "portal", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static BaseBlock portal;
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

    @Override
    public void onInitialize(){
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

        PortalGroupCapability.registerListeners();

        register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("wormhole");

        // Portal frame
        handler.registerBlock("portal_frame", () -> new PortalGroupBlock(() -> portal_frame_tile));
        handler.registerBlockEntityType("portal_frame_tile", () -> BaseBlockEntityType.create((pos, state) -> new PortalGroupBlockEntity(portal_frame_tile, pos, state), portal_frame));
        handler.registerItem("portal_frame", () -> new BaseBlockItem(portal_frame, ItemProperties.create().group(ITEM_GROUP)));

        // Portal
        handler.registerBlock("portal", PortalBlock::new);
        handler.registerBlockEntityType("portal_tile", () -> BaseBlockEntityType.create(PortalBlockEntity::new, portal));
        handler.registerItem("portal", () -> new BaseBlockItem(portal, ItemProperties.create()));

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

        // NBT recipe serializer
        handler.registerRecipeSerializer("nbtrecipe", NBTRecipe.SERIALIZER);
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
}
