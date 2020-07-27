package com.supermartijn642.wormhole;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
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
        this(world.provider.getDimensionType().getName(), pos.getX(), pos.getY(), pos.getZ(), yaw);
    }

    public PortalTarget(NBTTagCompound tag){
        this(tag.getString("dimension"), tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getFloat("yaw"));
    }

    public static PortalTarget read(NBTTagCompound tag){
        return new PortalTarget(tag);
    }

    public NBTTagCompound write(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("dimension", this.dimension);
        tag.setInteger("x", this.x);
        tag.setInteger("y", this.y);
        tag.setInteger("z", this.z);
        tag.setFloat("yaw", this.yaw);
        return tag;
    }

    public Optional<World> getWorld(MinecraftServer server){
        DimensionType type = DimensionType.byName(this.dimension);
        if(type == null)
            return Optional.empty();
        return Optional.of(server.getWorld(type.getId()));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

}
