package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.energycell.EnergyCellBlock;
import com.supermartijn642.wormhole.energycell.EnergyCellType;
import com.supermartijn642.wormhole.generator.CoalGeneratorBlock;
import com.supermartijn642.wormhole.generator.CoalGeneratorTile;
import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import com.supermartijn642.wormhole.portal.packets.*;
import com.supermartijn642.wormhole.targetcell.TargetCellBlock;
import com.supermartijn642.wormhole.targetcell.TargetCellType;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceMovePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceNamePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Comparator;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = Wormhole.MODID, name = Wormhole.NAME, version = Wormhole.VERSION, dependencies = Wormhole.DEPENDENCIES)
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

    public static final String MODID = "wormhole";
    public static final String NAME = "Wormhole (Portals)";
    public static final String VERSION = "1.1.7";
    public static final String DEPENDENCIES = "required-after:supermartijn642corelib@[1.0.3,1.1.0);required-after:supermartijn642configlib@[1.0.5,)";

    public static final CreativeTabs ITEM_GROUP = new CreativeTabs("wormhole") {
        @Override
        public ItemStack getTabIconItem(){
            return new ItemStack(advanced_target_device);
        }

        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items){
            super.displayAllRelevantItems(items);
            items.sort(Comparator.comparing(a -> a.getDisplayName()));
        }
    };

    @Mod.Instance
    public static Wormhole instance;

    public static SimpleNetworkWrapper channel;

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
    @GameRegistry.ObjectHolder("wormhole:basic_energy_cell")
    public static EnergyCellBlock basic_energy_cell;
    @GameRegistry.ObjectHolder("wormhole:advanced_energy_cell")
    public static EnergyCellBlock advanced_energy_cell;
    @GameRegistry.ObjectHolder("wormhole:creative_energy_cell")
    public static EnergyCellBlock creative_energy_cell;
    @GameRegistry.ObjectHolder("wormhole:target_device")
    public static Item target_device;
    @GameRegistry.ObjectHolder("wormhole:advanced_target_device")
    public static Item advanced_target_device;
    @GameRegistry.ObjectHolder("wormhole:basic_target_cell")
    public static TargetCellBlock basic_target_cell;
    @GameRegistry.ObjectHolder("wormhole:advanced_target_cell")
    public static TargetCellBlock advanced_target_cell;
    @GameRegistry.ObjectHolder("wormhole:coal_generator")
    public static CoalGeneratorBlock coal_generator;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        channel.registerMessage(TargetDeviceAddPacket.class, TargetDeviceAddPacket.class, 0, Side.SERVER);
        channel.registerMessage(TargetDeviceMovePacket.class, TargetDeviceMovePacket.class, 1, Side.SERVER);
        channel.registerMessage(TargetDeviceRemovePacket.class, TargetDeviceRemovePacket.class, 2, Side.SERVER);
        channel.registerMessage(TargetDeviceNamePacket.class, TargetDeviceNamePacket.class, 3, Side.SERVER);
        channel.registerMessage(PortalAddTargetPacket.class, PortalAddTargetPacket.class, 4, Side.SERVER);
        channel.registerMessage(PortalClearTargetPacket.class, PortalClearTargetPacket.class, 5, Side.SERVER);
        channel.registerMessage(PortalMoveTargetPacket.class, PortalMoveTargetPacket.class, 6, Side.SERVER);
        channel.registerMessage(PortalNameTargetPacket.class, PortalNameTargetPacket.class, 7, Side.SERVER);
        channel.registerMessage(PortalSelectTargetPacket.class, PortalSelectTargetPacket.class, 8, Side.SERVER);
        channel.registerMessage(UpdateGroupPacket.class, UpdateGroupPacket.class, 9, Side.CLIENT);
        channel.registerMessage(UpdateGroupsPacket.class, UpdateGroupsPacket.class, 10, Side.CLIENT);
        channel.registerMessage(PortalColorTargetPacket.class, PortalColorTargetPacket.class, 11, Side.SERVER);
        channel.registerMessage(PortalActivatePacket.class, PortalActivatePacket.class, 12, Side.SERVER);
        channel.registerMessage(PortalDeactivatePacket.class, PortalDeactivatePacket.class, 13, Side.SERVER);

        PortalGroupCapability.register();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @Mod.EventBusSubscriber
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new PortalGroupBlock("portal_frame", PortalFrameTile::new).setCreativeTab(Wormhole.ITEM_GROUP));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.X));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.Y));
            e.getRegistry().register(new PortalBlock(EnumFacing.Axis.Z));
            e.getRegistry().register(new StabilizerBlock());
            for(EnergyCellType type : EnergyCellType.values())
                e.getRegistry().register(new EnergyCellBlock(type));
            for(TargetCellType type : TargetCellType.values())
                e.getRegistry().register(new TargetCellBlock(type));
            e.getRegistry().register(new CoalGeneratorBlock());

            GameRegistry.registerTileEntity(PortalFrameTile.class, new ResourceLocation(MODID, "portal_frame_tile"));
            GameRegistry.registerTileEntity(PortalTile.class, new ResourceLocation(MODID, "portal_tile"));
            GameRegistry.registerTileEntity(StabilizerTile.class, new ResourceLocation(MODID, "stabilizer_tile"));
            for(EnergyCellType type : EnergyCellType.values())
                GameRegistry.registerTileEntity(type.getTileEntityClass(), new ResourceLocation(MODID, type.getRegistryName() + "_tile"));
            for(TargetCellType type : TargetCellType.values())
                GameRegistry.registerTileEntity(type.getTileEntityClass(), new ResourceLocation(MODID, type.getRegistryName() + "_tile"));
            GameRegistry.registerTileEntity(CoalGeneratorTile.class, new ResourceLocation(MODID, "coal_generator_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new ItemBlock(portal_frame).setRegistryName(portal_frame.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_x).setRegistryName(portal_x.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_y).setRegistryName(portal_y.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_z).setRegistryName(portal_z.getRegistryName()));
            e.getRegistry().register(new ItemBlock(portal_stabilizer).setRegistryName(portal_stabilizer.getRegistryName()));
            e.getRegistry().register(new ItemBlock(basic_energy_cell).setRegistryName(basic_energy_cell.getRegistryName()));
            e.getRegistry().register(new ItemBlock(advanced_energy_cell).setRegistryName(advanced_energy_cell.getRegistryName()));
            e.getRegistry().register(new ItemBlock(creative_energy_cell).setRegistryName(creative_energy_cell.getRegistryName()));
            e.getRegistry().register(new ItemBlock(basic_target_cell).setRegistryName(basic_target_cell.getRegistryName()));
            e.getRegistry().register(new ItemBlock(advanced_target_cell).setRegistryName(advanced_target_cell.getRegistryName()));
            e.getRegistry().register(new ItemBlock(coal_generator).setRegistryName(coal_generator.getRegistryName()));

            e.getRegistry().register(new TargetDeviceItem("target_device", () -> WormholeConfig.basicDeviceTargetCount.get()));
            e.getRegistry().register(new TargetDeviceItem("advanced_target_device", () -> WormholeConfig.advancedDeviceTargetCount.get()));
        }
    }

}
