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
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

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

    public static final IRecipeSerializer<NBTRecipe> NBT_RECIPE_SERIALIZER = new NBTRecipe.Serializer();

    @ObjectHolder("wormhole:portal_frame")
    public static Block portal_frame;
    @ObjectHolder("wormhole:portal")
    public static Block portal;
    @ObjectHolder("wormhole:portal_stabilizer")
    public static Block portal_stabilizer;
    @ObjectHolder("wormhole:basic_energy_cell")
    public static EnergyCellBlock basic_energy_cell;
    @ObjectHolder("wormhole:advanced_energy_cell")
    public static EnergyCellBlock advanced_energy_cell;
    @ObjectHolder("wormhole:creative_energy_cell")
    public static EnergyCellBlock creative_energy_cell;
    @ObjectHolder("wormhole:target_device")
    public static Item target_device;
    @ObjectHolder("wormhole:advanced_target_device")
    public static Item advanced_target_device;
    @ObjectHolder("wormhole:basic_target_cell")
    public static TargetCellBlock basic_target_cell;
    @ObjectHolder("wormhole:advanced_target_cell")
    public static TargetCellBlock advanced_target_cell;
    @ObjectHolder("wormhole:coal_generator")
    public static CoalGeneratorBlock coal_generator;

    @ObjectHolder("wormhole:portal_frame_tile")
    public static TileEntityType<?> portal_frame_tile;
    @ObjectHolder("wormhole:portal_tile")
    public static TileEntityType<?> portal_tile;
    @ObjectHolder("wormhole:stabilizer_tile")
    public static TileEntityType<?> stabilizer_tile;
    @ObjectHolder("wormhole:basic_energy_cell_tile")
    public static TileEntityType<EnergyCellTile> basic_energy_cell_tile;
    @ObjectHolder("wormhole:advanced_energy_cell_tile")
    public static TileEntityType<EnergyCellTile> advanced_energy_cell_tile;
    @ObjectHolder("wormhole:creative_energy_cell_tile")
    public static TileEntityType<EnergyCellTile> creative_energy_cell_tile;
    @ObjectHolder("wormhole:basic_target_cell_tile")
    public static TileEntityType<TargetCellTile> basic_target_cell_tile;
    @ObjectHolder("wormhole:advanced_target_cell_tile")
    public static TileEntityType<TargetCellTile> advanced_target_cell_tile;
    @ObjectHolder("wormhole:coal_generator_tile")
    public static TileEntityType<CoalGeneratorTile> coal_generator_tile;

    @ObjectHolder("wormhole:coal_generator_container")
    public static ContainerType<CoalGeneratorContainer> coal_generator_container;

    public Wormhole(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WormholeConfig.CONFIG_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);

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

    public void init(FMLCommonSetupEvent e){
        PortalGroupCapability.register();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new PortalGroupBlock("portal_frame", () -> new PortalGroupTile(portal_frame_tile)));
            e.getRegistry().register(new PortalBlock());
            e.getRegistry().register(new StabilizerBlock());
            for(EnergyCellType type : EnergyCellType.values())
                e.getRegistry().register(new EnergyCellBlock(type));
            for(TargetCellType type : TargetCellType.values())
                e.getRegistry().register(new TargetCellBlock(type));
            e.getRegistry().register(new CoalGeneratorBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(() -> new PortalGroupTile(portal_frame_tile), portal_frame).build(null).setRegistryName("portal_frame_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(PortalTile::new, portal).build(null).setRegistryName("portal_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(StabilizerTile::new, portal_stabilizer).build(null).setRegistryName("stabilizer_tile"));
            for(EnergyCellType type : EnergyCellType.values())
                e.getRegistry().register(TileEntityType.Builder.create(type::createTile, type.getBlock()).build(null).setRegistryName(type.getRegistryName() + "_tile"));
            for(TargetCellType type : TargetCellType.values())
                e.getRegistry().register(TileEntityType.Builder.create(type::createTile, type.getBlock()).build(null).setRegistryName(type.getRegistryName() + "_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(CoalGeneratorTile::new, coal_generator).build(null).setRegistryName("coal_generator_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(portal_frame, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(portal_frame.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal, new Item.Properties()).setRegistryName(portal.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal_stabilizer, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(portal_stabilizer.getRegistryName()));
            e.getRegistry().register(new BlockItem(basic_energy_cell, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(basic_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(advanced_energy_cell, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(advanced_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(creative_energy_cell, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(creative_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(basic_target_cell, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(basic_target_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(advanced_target_cell, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(advanced_target_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(coal_generator, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(coal_generator.getRegistryName()));

            e.getRegistry().register(new TargetDeviceItem("target_device", WormholeConfig.INSTANCE.basicDeviceTargetCount::get));
            e.getRegistry().register(new TargetDeviceItem("advanced_target_device", WormholeConfig.INSTANCE.advancedDeviceTargetCount::get));
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> e){
            e.getRegistry().register(IForgeContainerType.create((windowId, inv, data) -> new CoalGeneratorContainer(windowId, inv.player, data.readBlockPos())).setRegistryName("coal_generator_container"));
        }

        @SubscribeEvent
        public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> e){
            e.getRegistry().register(NBT_RECIPE_SERIALIZER.setRegistryName(new ResourceLocation("wormhole", "nbtrecipe")));
        }
    }

}
