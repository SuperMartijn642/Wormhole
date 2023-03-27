package com.supermartijn642.wormhole;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Created 25/03/2023 by SuperMartijn642
 */
public class PortalGroupCapabilitySaveData extends SavedData {

    private static final String IDENTIFIER = "wormhole_portal_groups";

    private final PortalGroupCapability capability;

    public static void init(ServerLevel level, PortalGroupCapability capability){
        level.getDataStorage().computeIfAbsent(tag -> {
            PortalGroupCapabilitySaveData saveData = new PortalGroupCapabilitySaveData(capability);
            saveData.load(tag);
            return saveData;
        }, () -> new PortalGroupCapabilitySaveData(capability), IDENTIFIER);
    }

    public PortalGroupCapabilitySaveData(PortalGroupCapability capability){
        this.capability = capability;
    }

    @Override
    public CompoundTag save(CompoundTag tag){
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
