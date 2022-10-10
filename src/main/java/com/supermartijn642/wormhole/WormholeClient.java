package com.supermartijn642.wormhole;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WormholeClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("wormhole");

        // Set translucent render type for the portal
        handler.registerBlockModelTranslucentRenderType(() -> Wormhole.portal);

        // Register container screen for the coal generator
        handler.registerContainerScreen(() -> Wormhole.coal_generator_container, container -> WidgetContainerScreen.of(new CoalGeneratorScreen(), container, true));
    }

    public static void openTargetDeviceScreen(InteractionHand hand, BlockPos pos, float yaw){
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
    public static void onBlockHighlight(RenderHighlightEvent.Block e){
        Level level = ClientUtils.getWorld();
        BlockEntity entity = level.getBlockEntity(e.getTarget().getBlockPos());
        if(entity instanceof GeneratorBlockEntity){
            PoseStack poseStack = e.getPoseStack();
            poseStack.pushPose();
            Vec3 playerPos = RenderUtils.getCameraPosition();
            poseStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
            for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingPortalBlocks()){
                VoxelShape shape = level.getBlockState(pos).getBlockSupportShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ());
                RenderUtils.renderShape(poseStack, shape, 66 / 255f, 108 / 255f, 245 / 255f, true);
            }
            for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingEnergyBlocks()){
                VoxelShape shape = level.getBlockState(pos).getBlockSupportShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ());
                RenderUtils.renderShape(poseStack, shape, 242 / 255f, 34 / 255f, 34 / 255f, true);
            }
            poseStack.popPose();
        }
    }
}
