package com.supermartijn642.wormhole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.gui.WidgetContainerScreen;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.wormhole.generator.CoalGeneratorScreen;
import com.supermartijn642.wormhole.generator.GeneratorBlockEntity;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public class WormholeClient {

    private static final ContextKey<GeneratorHighlightState> GENERATOR_HIGHLIGHT_DATA = new ContextKey<>(Identifier.fromNamespaceAndPath("wormhole", "generator_highlights"));
    private static final PoseStack POSE_STACK = new PoseStack();

    public static void register(){
        NeoForge.EVENT_BUS.addListener(WormholeClient::onBlockHighlightExtract);

        // Register container screen for the coal generator
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("wormhole");
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

    private static void onBlockHighlightExtract(ExtractBlockOutlineRenderStateEvent event){
        ClientLevel level = event.getLevel();
        BlockEntity entity = level.getBlockEntity(event.getHitResult().getBlockPos());
        if(!(entity instanceof GeneratorBlockEntity))
            return;

        GeneratorHighlightState state = new GeneratorHighlightState();
        for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingPortalBlocks()){
            VoxelShape shape = level.getBlockState(pos).getBlockSupportShape(level, pos);
            if(!shape.isEmpty())
                state.portalBlockShapes.add(Pair.of(pos, BlockShape.create(shape)));
        }
        for(BlockPos pos : ((GeneratorBlockEntity)entity).getChargingEnergyBlocks()){
            VoxelShape shape = level.getBlockState(pos).getBlockSupportShape(level, pos);
            if(!shape.isEmpty())
                state.energyBlockShapes.add(Pair.of(pos, BlockShape.create(shape)));
        }
        event.getLevelRenderState().setRenderData(GENERATOR_HIGHLIGHT_DATA, state);
        event.addCustomRenderer(WormholeClient::onBlockHighlightDraw);
    }

    private static boolean onBlockHighlightDraw(BlockOutlineRenderState outlineRenderState, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState){
        GeneratorHighlightState state = levelRenderState.getRenderData(GENERATOR_HIGHLIGHT_DATA);
        if(state == null)
            return true;

        POSE_STACK.pushPose();
        Vec3 playerPos = levelRenderState.cameraRenderState.pos;
        POSE_STACK.translate(-playerPos.x, -playerPos.y, -playerPos.z);

        for(Pair<BlockPos,BlockShape> block : state.portalBlockShapes){
            POSE_STACK.pushPose();
            BlockPos pos = block.left();
            POSE_STACK.translate(pos.getX(), pos.getY(), pos.getZ());
            RenderUtils.renderShape(POSE_STACK, block.right(), 66 / 255f, 108 / 255f, 245 / 255f, true);
            POSE_STACK.popPose();
        }
        for(Pair<BlockPos,BlockShape> block : state.energyBlockShapes){
            POSE_STACK.pushPose();
            BlockPos pos = block.left();
            POSE_STACK.translate(pos.getX(), pos.getY(), pos.getZ());
            RenderUtils.renderShape(POSE_STACK, block.right(), 242 / 255f, 34 / 255f, 34 / 255f, false);
            POSE_STACK.popPose();
        }

        POSE_STACK.popPose();
        return true;
    }

    private static class GeneratorHighlightState {
        List<Pair<BlockPos,BlockShape>> portalBlockShapes = new ArrayList<>();
        List<Pair<BlockPos,BlockShape>> energyBlockShapes = new ArrayList<>();
    }
}
