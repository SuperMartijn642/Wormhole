package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.TextComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTarget {

    public static final int MAX_NAME_LENGTH = 10;

    public final ResourceKey<Level> dimension;
    public final int x, y, z;
    public final float yaw;

    public String name;
    public DyeColor color = null;
    public Component dimensionDisplayName;

    public PortalTarget(ResourceKey<Level> dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;
        this.dimensionDisplayName = TextComponents.dimension(dimension).get();
    }

    public PortalTarget(Level level, BlockPos pos, float yaw, String name){
        this(level.dimension(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(CompoundTag tag){
        this(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension"))), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getFloat("yaw"), tag.contains("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : null;
    }

    public static PortalTarget read(CompoundTag tag){
        return new PortalTarget(tag);
    }

    public CompoundTag write(){
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", this.dimension.location().toString());
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        tag.putFloat("yaw", this.yaw);
        tag.putString("name", this.name);
        if(this.color != null)
            tag.putInt("color", this.color.getId());
        return tag;
    }

    public Optional<Level> getLevel(MinecraftServer server){
        return Optional.ofNullable(server.getLevel(this.dimension));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3 getCenteredPos(){
        return new Vec3(this.x + 0.5, this.y + 0.2, this.z + 0.5);
    }

    public Component getDimensionDisplayName(){
        return this.dimensionDisplayName;
    }
}
