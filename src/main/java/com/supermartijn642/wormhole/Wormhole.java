package com.supermartijn642.wormhole;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = Wormhole.MODID, name = Wormhole.NAME, version = Wormhole.VERSION)
public class Wormhole {

    public static final String MODID = "wormhole";
    public static final String NAME = "Wormhole (Portals)";
    public static final String VERSION = "1.0.9";

    @GameRegistry.ObjectHolder("wormhole:portal_frame")
    public static Block portal_frame;
    @GameRegistry.ObjectHolder("wormhole:portal_x")
    public static Block portal_x;
    @GameRegistry.ObjectHolder("wormhole:portal_y")
    public static Block portal_y;
    @GameRegistry.ObjectHolder("wormhole:portal_z")
    public static Block portal_z;
    @GameRegistry.ObjectHolder("wormhole:portal_stabilizer")
    public static Block portal_stabilizer;
    @GameRegistry.ObjectHolder("wormhole:target_device")
    public static Item target_device;

    @Mod.EventBusSubscriber
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new PortalGroupBlock("portal_frame", PortalFrameTile::new).setCreativeTab(CreativeTabs.SEARCH));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.X));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.Y));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.Z));
            e.getRegistry().register(new StabilizerBlock());
            GameRegistry.registerTileEntity(PortalFrameTile.class, new ResourceLocation(MODID, "portal_frame_tile"));
            GameRegistry.registerTileEntity(PortalTile.class, new ResourceLocation(MODID, "portal_tile"));
            GameRegistry.registerTileEntity(StabilizerTile.class, new ResourceLocation(MODID, "stabilizer_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new ItemBlock(portal_frame).setRegistryName(portal_frame.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_x).setRegistryName(portal_x.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_y).setRegistryName(portal_y.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_z).setRegistryName(portal_z.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_stabilizer).setRegistryName(portal_stabilizer.getRegistryName()));

            e.getRegistry().register(new TargetItem());
        }
    }

}
