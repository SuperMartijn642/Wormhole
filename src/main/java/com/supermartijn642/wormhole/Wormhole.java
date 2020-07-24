package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("wormhole")
public class Wormhole {

    @ObjectHolder("wormhole:portal_frame")
    public static Block portal_frame;
    @ObjectHolder("wormhole:portal")
    public static Block portal;
    @ObjectHolder("wormhole:portal_stabilizer")
    public static Block portal_stabilizer;
    @ObjectHolder("wormhole:target_device")
    public static Item target_device;

    @ObjectHolder("wormhole:portal_frame_tile")
    public static TileEntityType<?> portal_frame_tile;
    @ObjectHolder("wormhole:portal_tile")
    public static TileEntityType<?> portal_tile;
    @ObjectHolder("wormhole:stabilizer_tile")
    public static TileEntityType<?> stabilizer_tile;

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new PortalGroupBlock("portal_frame", () -> new PortalGroupTile(portal_frame_tile)));
            e.getRegistry().register(new PortalBlock());
            e.getRegistry().register(new StabilizerBlock());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> e){
            e.getRegistry().register(TileEntityType.Builder.create(() -> new PortalGroupTile(portal_frame_tile), portal_frame).build(null).setRegistryName("portal_frame_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(PortalTile::new, portal).build(null).setRegistryName("portal_tile"));
            e.getRegistry().register(TileEntityType.Builder.create(StabilizerTile::new, portal_stabilizer).build(null).setRegistryName("stabilizer_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new BlockItem(portal_frame, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(portal_frame.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(portal.getRegistryName()));
            e.getRegistry().register(new BlockItem(portal_stabilizer, new Item.Properties().group(ItemGroup.SEARCH)).setRegistryName(portal_stabilizer.getRegistryName()));

            e.getRegistry().register(new TargetItem());
        }
    }

}
