package com.supermartijn642.wormhole;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTarget {

    public final String dimension;
    public final int x, y, z;
    public final float yaw;

    public PortalTarget(String dimension, int x, int y, int z, float yaw){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public PortalTarget(World world, BlockPos pos, float yaw){
        this(world.func_234923_W_().func_240901_a_().toString(), pos.getX(), pos.getY(), pos.getZ(), yaw);
    }

    public PortalTarget(CompoundNBT tag){
        this(tag.getString("dimension"), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getFloat("yaw"));
    }

    public static PortalTarget read(CompoundNBT tag){
        return new PortalTarget(tag);
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putString("dimension", this.dimension);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        tag.putFloat("yaw", this.yaw);
        return tag;
    }

    public Optional<World> getWorld(MinecraftServer server){
        RegistryKey<World> key = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(this.dimension));
        return Optional.ofNullable(server.getWorld(key));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

}
