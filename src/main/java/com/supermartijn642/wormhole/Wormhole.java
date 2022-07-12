package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellType;
import com.supermartijn642.wormhole.generator.CoalGeneratorBlock;
import com.supermartijn642.wormhole.generator.CoalGeneratorContainer;
import com.supermartijn642.wormhole.generator.CoalGeneratorTile;
import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.packets.*;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellType;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceMovePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceNamePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("wormhole")
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

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("wormhole", "main"), () -> "1", "1"::equals, "1"::equals);

    public static final RecipeSerializer<NBTRecipe> NBT_RECIPE_SERIALIZER = new NBTRecipe.Serializer();
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("wormhole") {
        @Override
        public ItemStack makeIcon(){
            return new ItemStack(advanced_target_device);
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items){
            super.fillItemList(items);
            items.sort(Comparator.comparing(a -> a.getHoverName().getString()));
        }
    };

    @ObjectHolder(value = "wormhole:portal_frame", registryName = "minecraft:block")
    public static Block portal_frame;
    @ObjectHolder(value = "wormhole:portal", registryName = "minecraft:block")
    public static Block portal;
    @ObjectHolder(value = "wormhole:portal_stabilizer", registryName = "minecraft:block")
    public static Block portal_stabilizer;
    @ObjectHolder(value = "wormhole:basic_energy_cell", registryName = "minecraft:block")
    public static EnergyCellBlock basic_energy_cell;
    @ObjectHolder(value = "wormhole:advanced_energy_cell", registryName = "minecraft:block")
    public static EnergyCellBlock advanced_energy_cell;
    @ObjectHolder(value = "wormhole:creative_energy_cell", registryName = "minecraft:block")
    public static EnergyCellBlock creative_energy_cell;
    @ObjectHolder(value = "wormhole:target_device", registryName = "minecraft:item")
    public static Item target_device;
    @ObjectHolder(value = "wormhole:advanced_target_device", registryName = "minecraft:item")
    public static Item advanced_target_device;
    @ObjectHolder(value = "wormhole:basic_target_cell", registryName = "minecraft:block")
    public static TargetCellBlock basic_target_cell;
    @ObjectHolder(value = "wormhole:advanced_target_cell", registryName = "minecraft:block")
    public static TargetCellBlock advanced_target_cell;
    @ObjectHolder(value = "wormhole:coal_generator", registryName = "minecraft:block")
    public static CoalGeneratorBlock coal_generator;

    @ObjectHolder(value = "wormhole:portal_frame_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<?> portal_frame_tile;
    @ObjectHolder(value = "wormhole:portal_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<?> portal_tile;
    @ObjectHolder(value = "wormhole:stabilizer_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<?> stabilizer_tile;
    @ObjectHolder(value = "wormhole:basic_energy_cell_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<EnergyCellTile> basic_energy_cell_tile;
    @ObjectHolder(value = "wormhole:advanced_energy_cell_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<EnergyCellTile> advanced_energy_cell_tile;
    @ObjectHolder(value = "wormhole:creative_energy_cell_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<EnergyCellTile> creative_energy_cell_tile;
    @ObjectHolder(value = "wormhole:basic_target_cell_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<TargetCellTile> basic_target_cell_tile;
    @ObjectHolder(value = "wormhole:advanced_target_cell_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<TargetCellTile> advanced_target_cell_tile;
    @ObjectHolder(value = "wormhole:coal_generator_tile", registryName = "minecraft:block_entity_type")
    public static BlockEntityType<CoalGeneratorTile> coal_generator_tile;

    @ObjectHolder(value = "wormhole:coal_generator_container", registryName = "minecraft:menu")
    public static MenuType<CoalGeneratorContainer> coal_generator_container;

    public Wormhole(){
        CHANNEL.registerMessage(0, TargetDeviceAddPacket.class, TargetDeviceAddPacket::encode, TargetDeviceAddPacket::new, TargetDeviceAddPacket::handle);
        CHANNEL.registerMessage(1, TargetDeviceMovePacket.class, TargetDeviceMovePacket::encode, TargetDeviceMovePacket::new, TargetDeviceMovePacket::handle);
        CHANNEL.registerMessage(2, TargetDeviceRemovePacket.class, TargetDeviceRemovePacket::encode, TargetDeviceRemovePacket::new, TargetDeviceRemovePacket::handle);
        CHANNEL.registerMessage(3, TargetDeviceNamePacket.class, TargetDeviceNamePacket::encode, TargetDeviceNamePacket::new, TargetDeviceNamePacket::handle);
        CHANNEL.registerMessage(4, PortalAddTargetPacket.class, PortalAddTargetPacket::encode, PortalAddTargetPacket::new, PortalAddTargetPacket::handle);
        CHANNEL.registerMessage(5, PortalClearTargetPacket.class, PortalClearTargetPacket::encode, PortalClearTargetPacket::new, PortalClearTargetPacket::handle);
        CHANNEL.registerMessage(6, PortalMoveTargetPacket.class, PortalMoveTargetPacket::encode, PortalMoveTargetPacket::new, PortalMoveTargetPacket::handle);
        CHANNEL.registerMessage(7, PortalNameTargetPacket.class, PortalNameTargetPacket::encode, PortalNameTargetPacket::new, PortalNameTargetPacket::handle);
        CHANNEL.registerMessage(8, PortalSelectTargetPacket.class, PortalSelectTargetPacket::encode, PortalSelectTargetPacket::new, PortalSelectTargetPacket::handle);
        CHANNEL.registerMessage(9, UpdateGroupPacket.class, UpdateGroupPacket::encode, UpdateGroupPacket::new, UpdateGroupPacket::handle);
        CHANNEL.registerMessage(10, UpdateGroupsPacket.class, UpdateGroupsPacket::encode, UpdateGroupsPacket::new, UpdateGroupsPacket::handle);
        CHANNEL.registerMessage(11, PortalColorTargetPacket.class, PortalColorTargetPacket::encode, PortalColorTargetPacket::new, PortalColorTargetPacket::handle);
        CHANNEL.registerMessage(12, PortalActivatePacket.class, PortalActivatePacket::encode, PortalActivatePacket::new, PortalActivatePacket::handle);
        CHANNEL.registerMessage(13, PortalDeactivatePacket.class, PortalDeactivatePacket::encode, PortalDeactivatePacket::new, PortalDeactivatePacket::handle);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onRegisterEvent(RegisterEvent e){
            if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS))
                onBlockRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES))
                onTileRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
                onItemRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES))
                onContainerRegistry(Objects.requireNonNull(e.getForgeRegistry()));
            else if(e.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
                onRecipeRegistry(Objects.requireNonNull(e.getForgeRegistry()));
        }

        public static void onBlockRegistry(IForgeRegistry<Block> registry){
            registry.register(new ResourceLocation("wormhole", "portal_frame"), new PortalGroupBlock("portal_frame", (pos, state) -> new PortalGroupTile(portal_frame_tile, pos, state)));
            registry.register(new ResourceLocation("wormhole", "portal"), new PortalBlock());
            registry.register(new ResourceLocation("wormhole", "portal_stabilizer"), new StabilizerBlock());
            for(EnergyCellType type : EnergyCellType.values())
                registry.register(new ResourceLocation("wormhole", type.getRegistryName()), new EnergyCellBlock(type));
            for(TargetCellType type : TargetCellType.values())
                registry.register(new ResourceLocation("wormhole", type.getRegistryName()), new TargetCellBlock(type));
            registry.register(new ResourceLocation("wormhole", "coal_generator"), new CoalGeneratorBlock());
        }

        public static void onTileRegistry(IForgeRegistry<BlockEntityType<?>> registry){
            registry.register(new ResourceLocation("wormhole", "portal_frame_tile"), BlockEntityType.Builder.of((pos, state) -> new PortalGroupTile(portal_frame_tile, pos, state), portal_frame).build(null));
            registry.register(new ResourceLocation("wormhole", "portal_tile"), BlockEntityType.Builder.of(PortalTile::new, portal).build(null));
            registry.register(new ResourceLocation("wormhole", "stabilizer_tile"), BlockEntityType.Builder.of(StabilizerTile::new, portal_stabilizer).build(null));
            for(EnergyCellType type : EnergyCellType.values())
                registry.register(new ResourceLocation("wormhole", type.getRegistryName() + "_tile"), BlockEntityType.Builder.of(type::createTile, type.getBlock()).build(null));
            for(TargetCellType type : TargetCellType.values())
                registry.register(new ResourceLocation("wormhole", type.getRegistryName() + "_tile"), BlockEntityType.Builder.of(type::createTile, type.getBlock()).build(null));
            registry.register(new ResourceLocation("wormhole", "coal_generator_tile"), BlockEntityType.Builder.of(CoalGeneratorTile::new, coal_generator).build(null));
        }

        public static void onItemRegistry(IForgeRegistry<Item> registry){
            registry.register(new ResourceLocation("wormhole", "portal_frame"), new BlockItem(portal_frame, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", "portal"), new BlockItem(portal, new Item.Properties()));
            registry.register(new ResourceLocation("wormhole", "portal_stabilizer"), new BlockItem(portal_stabilizer, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", EnergyCellType.BASIC.getRegistryName()), new BlockItem(basic_energy_cell, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", EnergyCellType.ADVANCED.getRegistryName()), new BlockItem(advanced_energy_cell, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", EnergyCellType.CREATIVE.getRegistryName()), new BlockItem(creative_energy_cell, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", TargetCellType.BASIC.getRegistryName()), new BlockItem(basic_target_cell, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", TargetCellType.ADVANCED.getRegistryName()), new BlockItem(advanced_target_cell, new Item.Properties().tab(ITEM_GROUP)));
            registry.register(new ResourceLocation("wormhole", "coal_generator"), new BlockItem(coal_generator, new Item.Properties().tab(ITEM_GROUP)));

            registry.register(new ResourceLocation("wormhole", "target_device"), new TargetDeviceItem("target_device", WormholeConfig.basicDeviceTargetCount::get));
            registry.register(new ResourceLocation("wormhole", "advanced_target_device"), new TargetDeviceItem("advanced_target_device", WormholeConfig.advancedDeviceTargetCount::get));
        }

        public static void onContainerRegistry(IForgeRegistry<MenuType<?>> registry){
            registry.register(new ResourceLocation("wormhole", "coal_generator_container"), IForgeMenuType.create((windowId, inv, data) -> new CoalGeneratorContainer(windowId, inv.player, data.readBlockPos())));
        }

        public static void onRecipeRegistry(IForgeRegistry<RecipeSerializer<?>> registry){
            registry.register(new ResourceLocation("wormhole", "nbtrecipe"), NBT_RECIPE_SERIALIZER);
        }
    }

}
