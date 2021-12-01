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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Comparator;

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
    public static BlockEntityType<?> portal_frame_tile;
    @ObjectHolder("wormhole:portal_tile")
    public static BlockEntityType<?> portal_tile;
    @ObjectHolder("wormhole:stabilizer_tile")
    public static BlockEntityType<?> stabilizer_tile;
    @ObjectHolder("wormhole:basic_energy_cell_tile")
    public static BlockEntityType<EnergyCellTile> basic_energy_cell_tile;
    @ObjectHolder("wormhole:advanced_energy_cell_tile")
    public static BlockEntityType<EnergyCellTile> advanced_energy_cell_tile;
    @ObjectHolder("wormhole:creative_energy_cell_tile")
    public static BlockEntityType<EnergyCellTile> creative_energy_cell_tile;
    @ObjectHolder("wormhole:basic_target_cell_tile")
    public static BlockEntityType<TargetCellTile> basic_target_cell_tile;
    @ObjectHolder("wormhole:advanced_target_cell_tile")
    public static BlockEntityType<TargetCellTile> advanced_target_cell_tile;
    @ObjectHolder("wormhole:coal_generator_tile")
    public static BlockEntityType<CoalGeneratorTile> coal_generator_tile;

    @ObjectHolder("wormhole:coal_generator_container")
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
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new PortalGroupBlock("portal_frame", (pos, state) -> new PortalGroupTile(portal_frame_tile, pos, state)));
            e.getRegistry().register(new PortalBlock());
            e.getRegistry().register(new StabilizerBlock());
            for(EnergyCellType type : EnergyCellType.values())
                e.getRegistry().register(new EnergyCellBlock(type));
            for(TargetCellType type : TargetCellType.values())
                e.getRegistry().register(new TargetCellBlock(type));
            e.getRegistry().register(new CoalGeneratorBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<BlockEntityType<?>> e){
            e.getRegistry().register(BlockEntityType.Builder.of((pos, state) -> new PortalGroupTile(portal_frame_tile, pos, state), portal_frame).build(null).setRegistryName("portal_frame_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(PortalTile::new, portal).build(null).setRegistryName("portal_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(StabilizerTile::new, portal_stabilizer).build(null).setRegistryName("stabilizer_tile"));
            for(EnergyCellType type : EnergyCellType.values())
                e.getRegistry().register(BlockEntityType.Builder.of(type::createTile, type.getBlock()).build(null).setRegistryName(type.getRegistryName() + "_tile"));
            for(TargetCellType type : TargetCellType.values())
                e.getRegistry().register(BlockEntityType.Builder.of(type::createTile, type.getBlock()).build(null).setRegistryName(type.getRegistryName() + "_tile"));
            e.getRegistry().register(BlockEntityType.Builder.of(CoalGeneratorTile::new, coal_generator).build(null).setRegistryName("coal_generator_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(portal_frame, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(portal_frame.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal, new Item.Properties()).setRegistryName(portal.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal_stabilizer, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(portal_stabilizer.getRegistryName()));
            e.getRegistry().register(new BlockItem(basic_energy_cell, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(basic_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(advanced_energy_cell, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(advanced_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(creative_energy_cell, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(creative_energy_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(basic_target_cell, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(basic_target_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(advanced_target_cell, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(advanced_target_cell.getRegistryName()));
            e.getRegistry().register(new BlockItem(coal_generator, new Item.Properties().tab(ITEM_GROUP)).setRegistryName(coal_generator.getRegistryName()));

            e.getRegistry().register(new TargetDeviceItem("target_device", WormholeConfig.basicDeviceTargetCount::get));
            e.getRegistry().register(new TargetDeviceItem("advanced_target_device", WormholeConfig.advancedDeviceTargetCount::get));
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<MenuType<?>> e){
            e.getRegistry().register(IForgeMenuType.create((windowId, inv, data) -> new CoalGeneratorContainer(windowId, inv.player, data.readBlockPos())).setRegistryName("coal_generator_container"));
        }

        @SubscribeEvent
        public static void onRecipeRegistry(final RegistryEvent.Register<RecipeSerializer<?>> e){
            e.getRegistry().register(NBT_RECIPE_SERIALIZER.setRegistryName(new ResourceLocation("wormhole", "nbtrecipe")));
        }
    }

}
