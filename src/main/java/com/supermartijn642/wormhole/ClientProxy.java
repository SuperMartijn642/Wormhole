package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void clientSetupEvent(FMLClientSetupEvent e){
        RenderTypeLookup.setRenderLayer(Wormhole.portal, RenderType.getTranslucent());

        ClientRegistry.bindTileEntityRenderer(Wormhole.basic_energy_cell_tile, EnergyCellTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Wormhole.advanced_energy_cell_tile, EnergyCellTileRenderer::new);
    }

    @SubscribeEvent
    public static void modelRegistry(ModelRegistryEvent e){
        for(ResourceLocation model : EnergyCellTileRenderer.ENERGY_CELL_MODELS)
            ModelLoader.addSpecialModel(model);
    }

    public static void openTargetDeviceScreen(Hand hand, BlockPos pos, float yaw){
        Minecraft.getInstance().displayGuiScreen(new TargetDeviceScreen(getPlayer(), hand, pos, yaw));
    }

    public static void openPortalTargetScreen(BlockPos pos){
        Minecraft.getInstance().displayGuiScreen(new PortalTargetScreen(pos, getPlayer()));
    }

    public static void openPortalTargetScreen(BlockPos pos, int scrollOffset){
        Minecraft.getInstance().displayGuiScreen(new PortalTargetScreen(pos, scrollOffset, getPlayer()));
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

}
