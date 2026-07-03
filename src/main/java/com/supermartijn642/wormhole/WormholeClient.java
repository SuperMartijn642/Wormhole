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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class WormholeClient {

    private static final PoseStack POSE_STACK = new PoseStack();

    public static void register(){
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

    @SubscribeEvent
    private static void onBlockHighlightExtract(RenderHighlightEvent.Block event){
        Level level = ClientUtils.getWorld();
        BlockEntity entity = level.getBlockEntity(event.getTarget().getBlockPos());
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
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        //noinspection deprecation
        BlockOutlineRenderState outlineRenderState = new BlockOutlineRenderState(
            pos,
            ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(blockState).hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT),
            ClientUtils.getMinecraft().options.highContrastBlockOutline().get(),
            blockState.getShape(level, pos, CollisionContext.of(event.getCamera().entity()))
        );
        LevelRenderer levelRenderer = event.getLevelRenderer();
        event.setCustomRenderer((source, stack, translucent, levelRenderState) -> onBlockHighlightDraw(outlineRenderState, source, stack, translucent, levelRenderState, levelRenderer, state));
    }

    private static boolean onBlockHighlightDraw(BlockOutlineRenderState outlineRenderState, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState, LevelRenderer levelRenderer, GeneratorHighlightState state){
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

        // Render original outline
        BlockOutlineRenderState temp = levelRenderState.blockOutlineRenderState;
        levelRenderState.blockOutlineRenderState = outlineRenderState;
        levelRenderer.renderBlockOutline(bufferSource, poseStack, translucentPass, levelRenderState);
        levelRenderState.blockOutlineRenderState = temp;
        return true;
    }

    private static class GeneratorHighlightState {
        List<Pair<BlockPos,BlockShape>> portalBlockShapes = new ArrayList<>();
        List<Pair<BlockPos,BlockShape>> energyBlockShapes = new ArrayList<>();
    }
}
