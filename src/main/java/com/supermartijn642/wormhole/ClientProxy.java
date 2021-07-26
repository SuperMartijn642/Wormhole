package com.supermartijn642.wormhole;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.wormhole.energycell.EnergyCellTileRenderer;
import com.supermartijn642.wormhole.generator.CoalGeneratorContainer;
import com.supermartijn642.wormhole.generator.CoalGeneratorScreen;
import com.supermartijn642.wormhole.generator.GeneratorTile;
import com.supermartijn642.wormhole.portal.screen.PortalOverviewScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetColorScreen;
import com.supermartijn642.wormhole.portal.screen.PortalTargetScreen;
import com.supermartijn642.wormhole.targetcell.TargetCellTileRenderer;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
        RenderTypeLookup.setRenderLayer(Wormhole.portal, RenderType.translucent());

        ClientRegistry.bindTileEntityRenderer(Wormhole.basic_energy_cell_tile, EnergyCellTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Wormhole.advanced_energy_cell_tile, EnergyCellTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Wormhole.basic_target_cell_tile, TargetCellTileRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Wormhole.advanced_target_cell_tile, TargetCellTileRenderer::new);

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
        ScreenManager.register(Wormhole.coal_generator_container, (ScreenManager.IScreenFactory<CoalGeneratorContainer,CoalGeneratorScreen>)((container, player, title) -> new CoalGeneratorScreen(container, player)));
    }

    public static void openTargetDeviceScreen(Hand hand, BlockPos pos, float yaw){
        Minecraft.getInstance().setScreen(new TargetDeviceScreen(getPlayer(), hand, pos, yaw));
    }

    public static void openPortalTargetScreen(BlockPos pos){
        Minecraft.getInstance().setScreen(new PortalTargetScreen(pos, getPlayer()));
    }

    public static void openPortalTargetScreen(BlockPos pos, int scrollOffset, int selectedPortalTarget, int selectedDeviceTarget){
        Minecraft.getInstance().setScreen(new PortalTargetScreen(pos, getPlayer(), scrollOffset, selectedPortalTarget, selectedDeviceTarget));
    }

    public static void openPortalTargetColorScreen(BlockPos pos, int targetIndex, Runnable returnScreen){
        Minecraft.getInstance().setScreen(new PortalTargetColorScreen(pos, targetIndex, returnScreen));
    }

    public static void openPortalOverviewScreen(BlockPos pos){
        Minecraft.getInstance().setScreen(new PortalOverviewScreen(pos));
    }

    public static PlayerEntity getPlayer(){
        return Minecraft.getInstance().player;
    }

    public static World getWorld(){
        return Minecraft.getInstance().level;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawHighlightEvent.HighlightBlock e){
            World world = getWorld();
            TileEntity tile = world.getBlockEntity(e.getTarget().getBlockPos());
            if(tile instanceof GeneratorTile){
                MatrixStack matrixStack = e.getMatrix();
                matrixStack.pushPose();
                Vector3d playerPos = Minecraft.getInstance().player.getEyePosition(e.getPartialTicks());
                matrixStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
                IVertexBuilder builder = e.getBuffers().getBuffer(RenderType.lines());
                for(BlockPos pos : ((GeneratorTile)tile).getChargingPortalBlocks()){
                    VoxelShape shape = world.getBlockState(pos).getBlockSupportShape(world, pos);
                    drawShape(e.getMatrix(), builder, shape, pos.getX(), pos.getY(), pos.getZ(), 66 / 255f, 108 / 255f, 245 / 255f, 1);
                }
                for(BlockPos pos : ((GeneratorTile)tile).getChargingEnergyBlocks()){
                    VoxelShape shape = world.getBlockState(pos).getBlockSupportShape(world, pos);
                    drawShape(e.getMatrix(), builder, shape, pos.getX(), pos.getY(), pos.getZ(), 242 / 255f, 34 / 255f, 34 / 255f, 1);
                }
                matrixStack.popPose();
            }
        }

        private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Matrix4f matrix4f = matrixStackIn.last().pose();
            shapeIn.forAllEdges((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
                bufferIn.vertex(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).endVertex();
                bufferIn.vertex(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).endVertex();
            });
        }
    }
}
