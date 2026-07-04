package com.supermartijn642.wormhole.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.wormhole.extensions.RenderTypeExtension;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 26/06/2026 by SuperMartijn642
 */
@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {

    @Unique
    private static final Vector3f NO_OFFSET = new Vector3f(DynamicUniforms.NO_OFFSET);

    @Unique
    private Vector4f colorModulator;

    @Final
    @Shadow
    private RenderSetup state;

    @Override
    public void wormholeSetColorModulator(Vector4f color){
        this.colorModulator = color;
    }

    @Inject(
        method = "writeDynamicTransforms",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void modifyColorModulator(Matrix4f modelViewMatrix, CallbackInfoReturnable<GpuBufferSlice> ci){
        if(this.colorModulator != null)
            ci.setReturnValue(RenderSystem.getDynamicUniforms().writeTransform(modelViewMatrix, this.colorModulator, NO_OFFSET, this.state.textureTransform.createMatrix()));
    }
}
