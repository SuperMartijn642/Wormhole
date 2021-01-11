package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.generator.GeneratorTile;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_frame), 0, new ModelResourceLocation(Wormhole.portal_frame.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_x), 0, new ModelResourceLocation(Wormhole.portal_x.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_y), 0, new ModelResourceLocation(Wormhole.portal_y.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_z), 0, new ModelResourceLocation(Wormhole.portal_z.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_stabilizer), 0, new ModelResourceLocation(Wormhole.portal_stabilizer.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.basic_energy_cell), 0, new ModelResourceLocation(Wormhole.basic_energy_cell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.advanced_energy_cell), 0, new ModelResourceLocation(Wormhole.advanced_energy_cell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.creative_energy_cell), 0, new ModelResourceLocation(Wormhole.creative_energy_cell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.basic_target_cell), 0, new ModelResourceLocation(Wormhole.basic_target_cell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.advanced_target_cell), 0, new ModelResourceLocation(Wormhole.advanced_target_cell.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.coal_generator), 0, new ModelResourceLocation(Wormhole.coal_generator.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Wormhole.target_device, 0, new ModelResourceLocation(Wormhole.target_device.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Wormhole.advanced_target_device, 0, new ModelResourceLocation(Wormhole.advanced_target_device.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(EnergyCellTile.BasicEnergyCellTile.class, new EnergyCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(EnergyCellTile.AdvancedEnergyCellTile.class, new EnergyCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TargetCellTile.BasicTargetCellTile.class, new TargetCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TargetCellTile.AdvancedTargetCellTile.class, new TargetCellTileRenderer());

//        ClientRegistry.bindTileEntityRenderer(Wormhole.coal_generator_tile, r -> new GeneratorTileRenderer<>(r, Color.BLUE));
    }

    public static void openTargetDeviceScreen(EnumHand hand, BlockPos pos, float yaw){
        Minecraft.getMinecraft().displayGuiScreen(new TargetDeviceScreen(getPlayer(), hand, pos, yaw));
    }

    public static void openPortalTargetScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new PortalTargetScreen(pos, getPlayer()));
    }

    public static void openPortalTargetScreen(BlockPos pos, int scrollOffset){
        Minecraft.getMinecraft().displayGuiScreen(new PortalTargetScreen(pos, scrollOffset, getPlayer()));
    }

    public static void openPortalTargetColorScreen(BlockPos pos, int targetIndex, Runnable returnScreen){
        Minecraft.getMinecraft().displayGuiScreen(new PortalTargetColorScreen(pos, targetIndex, returnScreen));
    }

    public static void openPortalOverviewScreen(BlockPos pos){
        Minecraft.getMinecraft().displayGuiScreen(new PortalOverviewScreen(pos));
    }

    public static EntityPlayer getPlayer(){
        return Minecraft.getMinecraft().player;
    }

    public static World getWorld(){
        return Minecraft.getMinecraft().world;
    }

    public static void queTask(Runnable task){
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent e){
            World world = getWorld();
            BlockPos generatorPos = e.getTarget().getBlockPos();
            // apparent this can be null github #11
            if(generatorPos != null){
                TileEntity tile = world.getTileEntity(e.getTarget().getBlockPos());
                if(tile instanceof GeneratorTile){
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(2.0F);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    EntityPlayer player = e.getPlayer();
                    float partialTicks = e.getPartialTicks();
                    double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                    double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                    double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                    GlStateManager.translate(-d3, -d4, -d5);
                    for(BlockPos pos : ((GeneratorTile)tile).getChargingPortalBlocks()){
                        AxisAlignedBB shape = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
                        drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 66 / 255f, 108 / 255f, 245 / 255f, 1);
                    }
                    for(BlockPos pos : ((GeneratorTile)tile).getChargingEnergyBlocks()){
                        AxisAlignedBB shape = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
                        drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 242 / 255f, 34 / 255f, 34 / 255f, 1);
                    }
                    GlStateManager.depthMask(true);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        }

        private static void drawShape(AxisAlignedBB shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            RenderGlobal.drawSelectionBoundingBox(shapeIn, red, green, blue, alpha);
        }
    }
}
