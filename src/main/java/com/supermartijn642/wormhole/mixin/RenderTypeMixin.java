package com.supermartijn642.wormhole.mixin;

import com.supermartijn642.wormhole.extensions.RenderTypeExtension;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Created 26/06/2026 by SuperMartijn642
 */
@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {

    @Unique
    private Vector4fc colorModulator;
    @Unique
    private final Vector4f dummyColor = new Vector4f();

    @Override
    public void setColorModulator(Vector4fc color){
        this.colorModulator = color;
    }

    @ModifyArg(
        method = "draw",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
        ),
        index = 1
    )
    private Vector4fc modifyColorModulator(Vector4fc original){
        return this.colorModulator == null ? original : this.dummyColor.set(original).mul(this.colorModulator);
    }
}
