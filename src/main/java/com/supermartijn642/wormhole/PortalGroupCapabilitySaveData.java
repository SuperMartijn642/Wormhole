package com.supermartijn642.wormhole;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class PortalGroupCapabilitySaveData extends SavedData {

    private static final String IDENTIFIER = "wormhole_portal_groups";

    private final PortalGroupCapability capability;

    public static void init(ServerLevel level, PortalGroupCapability capability){
        level.getDataStorage().computeIfAbsent(new SavedDataType<>(
            IDENTIFIER,
            () -> new PortalGroupCapabilitySaveData(capability),
            new Codec<>() {
                @Override
                public <T> DataResult<Pair<PortalGroupCapabilitySaveData,T>> decode(DynamicOps<T> ops, T input){
                    try{
                        PortalGroupCapabilitySaveData saveData = new PortalGroupCapabilitySaveData(capability);
                        saveData.load((CompoundTag)ops.convertTo(NbtOps.INSTANCE, input));
                        return DataResult.success(Pair.of(saveData, input));
                    }catch(Exception e){
                        return DataResult.error(e::getMessage);
                    }
                }

                @Override
                public <T> DataResult<T> encode(PortalGroupCapabilitySaveData input, DynamicOps<T> ops, T prefix){
                    try{
                        return DataResult.success(NbtOps.INSTANCE.convertTo(ops, input.save()));
                    }catch(Exception e){
                        return DataResult.error(e::getMessage);
                    }
                }
            },
            null
        ));
    }

    public PortalGroupCapabilitySaveData(PortalGroupCapability capability){
        this.capability = capability;
    }

    public CompoundTag save(){
        return this.capability.write();
    }

    public void load(CompoundTag tag){
        this.capability.read(tag);
    }

    @Override
    public boolean isDirty(){
        return true;
    }
}
