package com.supermartijn642.wormhole;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.wormhole.energycell.EnergyCellTile;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.generator.CoalGeneratorContainer;
import com.supermartijn642.wormhole.generator.CoalGeneratorScreen;
import com.supermartijn642.wormhole.generator.GeneratorTile;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetcell.TargetCellTile;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void clientSetupEvent(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntitySpecialRenderer(EnergyCellTile.BasicEnergyCellTile.class, new EnergyCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(EnergyCellTile.AdvancedEnergyCellTile.class, new EnergyCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TargetCellTile.BasicTargetCellTile.class, new TargetCellTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TargetCellTile.AdvancedTargetCellTile.class, new TargetCellTileRenderer());

//        ClientRegistry.bindTileEntityRenderer(Wormhole.coal_generator_tile, r -> new GeneratorTileRenderer<>(r, Color.BLUE));

        registerScreen();
    }

    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent e){
        for(ResourceLocation model : EnergyCellTileRenderer.ENERGY_CELL_MODELS)
            ModelLoader.addSpecialModel(model);
        for(ResourceLocation model : TargetCellTileRenderer.BASIC_TARGET_CELL_MODELS)
            ModelLoader.addSpecialModel(model);
        for(ResourceLocation model : TargetCellTileRenderer.ADVANCED_TARGET_CELL_MODELS)
            ModelLoader.addSpecialModel(model);
    }

    public static void registerScreen(){
        ScreenManager.registerFactory(Wormhole.coal_generator_container, (ScreenManager.IScreenFactory<CoalGeneratorContainer,CoalGeneratorScreen>)((container, player, title) -> new CoalGeneratorScreen(container, player)));
    }

    public static void openTargetDeviceScreen(Hand hand, BlockPos pos, float yaw){
        Minecraft.getInstance().displayGuiScreen(new TargetDeviceScreen(getPlayer(), hand, pos, yaw));
    }

    public static void openPortalTargetScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new PortalTargetScreen(pos, getPlayer()));
    }

    public static void openPortalTargetScreen(BlockPos pos, int scrollOffset, int selectedPortalTarget, int selectedDeviceTarget){
        Minecraft.getInstance().displayGuiScreen(new PortalTargetScreen(pos, getPlayer(), scrollOffset, selectedPortalTarget, selectedDeviceTarget));
    }

    public static void openPortalTargetColorScreen(BlockPos pos, int targetIndex, Runnable returnScreen){
        Minecraft.getInstance().displayGuiScreen(new PortalTargetColorScreen(pos, targetIndex, returnScreen));
    }

    public static void openPortalOverviewScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new PortalOverviewScreen(pos));
    }

    public static PlayerEntity getPlayer(){
        return Minecraft.getInstance().player;
    }

    public static World getWorld(){
        return Minecraft.getInstance().world;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent.HighlightBlock e){
            World world = getWorld();
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof GeneratorTile){
                GlStateManager.pushMatrix();
                GlStateManager.disableTexture();
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                Vec3d playerPos = Minecraft.getInstance().player.getEyePosition(e.getPartialTicks());
                GlStateManager.translated(-playerPos.x, -playerPos.y, -playerPos.z);
                for(BlockPos pos : ((GeneratorTile)tile).getChargingPortalBlocks()){
                    VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
                    drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 66 / 255f, 108 / 255f, 245 / 255f, 1);
                }
                for(BlockPos pos : ((GeneratorTile)tile).getChargingEnergyBlocks()){
                    VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
                    drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 242 / 255f, 34 / 255f, 34 / 255f, 1);
                }
                GlStateManager.popMatrix();
            }
        }

        private static void drawShape(VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
            shapeIn.forEachEdge((p_195468_11_, p_195468_13_, p_195468_15_, p_195468_17_, p_195468_19_, p_195468_21_) -> {
                bufferbuilder.pos(p_195468_11_ + xIn, p_195468_13_ + yIn, p_195468_15_ + zIn).color(red, green, blue, alpha).endVertex();
                bufferbuilder.pos(p_195468_17_ + xIn, p_195468_19_ + yIn, p_195468_21_ + zIn).color(red, green, blue, alpha).endVertex();
            });
            tessellator.draw();
        }
    }
}
