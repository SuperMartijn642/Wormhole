package com.supermartijn642.wormhole.mixin;

import com.supermartijn642.wormhole.PortalGroupCapability;
import com.supermartijn642.wormhole.extensions.WormholeLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
@Mixin(Level.class)
public class LevelMixin implements WormholeLevel {

    @Unique
    private PortalGroupCapability capability;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void constructor(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i, CallbackInfo ci){
        //noinspection DataFlowIssue
        Level level = (Level)(Object)this;
        this.capability = new PortalGroupCapability(level);
    }

    @Override
    public PortalGroupCapability wormholeGetPortalGroupCapability(){
        return this.capability;
    }
}
