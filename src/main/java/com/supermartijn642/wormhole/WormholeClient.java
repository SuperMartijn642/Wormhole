package com.supermartijn642.wormhole;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetContainerScreen;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.wormhole.generator.CoalGeneratorScreen;
import com.supermartijn642.wormhole.generator.GeneratorBlockEntity;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class WormholeClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("wormhole");

        // Set translucent render type for the portal
        handler.registerBlockModelTranslucentRenderType(() -> Wormhole.portal_x);
        handler.registerBlockModelTranslucentRenderType(() -> Wormhole.portal_y);
        handler.registerBlockModelTranslucentRenderType(() -> Wormhole.portal_z);

        // Register container screen for the coal generator
        handler.registerContainerScreen(() -> Wormhole.coal_generator_container, container -> WidgetContainerScreen.of(new CoalGeneratorScreen(), container, true));
    }

    public static void openTargetDeviceScreen(EnumHand hand, BlockPos pos, float yaw){
        ClientUtils.displayScreen(WidgetScreen.of(new TargetDeviceScreen(hand, pos, yaw)));
    }

    public static void openPortalTargetScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new PortalTargetScreen(pos)));
    }

    public static void openPortalTargetScreen(BlockPos pos, int scrollOffset, int selectedPortalTarget, int selectedDeviceTarget){
        ClientUtils.displayScreen(WidgetScreen.of(new PortalTargetScreen(pos, scrollOffset, selectedPortalTarget, selectedDeviceTarget)));
    }

    public static void openPortalTargetColorScreen(BlockPos pos, int targetIndex, Runnable returnScreen){
        ClientUtils.displayScreen(WidgetScreen.of(new PortalTargetColorScreen(pos, targetIndex, returnScreen)));
    }

    public static void openPortalOverviewScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new PortalOverviewScreen(pos)));
    }

    @SubscribeEvent
    public static void onBlockHighlight(DrawBlockHighlightEvent e){
        if(e.getTarget().typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        World level = ClientUtils.getWorld();
        TileEntity entity = level.getTileEntity(e.getTarget().getBlockPos());
        if(entity instanceof GeneratorBlockEntity){
            GlStateManager.pushMatrix();
            Vec3d playerPos = RenderUtils.getCameraPosition();
            GlStateManager.translate(-playerPos.x, -playerPos.y, -playerPos.z);
            for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingPortalBlocks()){
                AxisAlignedBB shape = level.getBlockState(pos).getSelectedBoundingBox(level, pos).offset(pos.getX(), pos.getY(), pos.getZ());
                RenderUtils.renderBox(shape, 66 / 255f, 108 / 255f, 245 / 255f, true);
            }
            for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingEnergyBlocks()){
                AxisAlignedBB shape = level.getBlockState(pos).getSelectedBoundingBox(level, pos).offset(pos.getX(), pos.getY(), pos.getZ());
                RenderUtils.renderBox(shape, 242 / 255f, 34 / 255f, 34 / 255f, true);
            }
            GlStateManager.popMatrix();
        }
    }
}
