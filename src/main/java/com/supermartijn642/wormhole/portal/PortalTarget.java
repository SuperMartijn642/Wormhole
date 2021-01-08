package com.supermartijn642.wormhole.portal;

import net.minecraft.item.EnumDyeColor;
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

    public static final int MAX_NAME_LENGTH = 10;

    public final int dimension;
    public final int x, y, z;
    public final float yaw;

    public String name;
    public EnumDyeColor color = null;

    public PortalTarget(int dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;
    }

    public PortalTarget(World world, BlockPos pos, float yaw, String name){
        this(world.provider.getDimensionType().getId(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(NBTTagCompound tag){
        this(tag.getInteger("dimension"), tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getFloat("yaw"), tag.hasKey("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.hasKey("color") ? EnumDyeColor.byDyeDamage(tag.getInteger("color")) : null;
    }

    public static PortalTarget read(NBTTagCompound tag){
        return new PortalTarget(tag);
    }

    public NBTTagCompound write(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", this.dimension);
        tag.setInteger("x", this.x);
        tag.setInteger("y", this.y);
        tag.setInteger("z", this.z);
        tag.setFloat("yaw", this.yaw);
        tag.setString("name", this.name);
        if(this.color != null)
            tag.setInteger("color", this.color.getDyeDamage());
        return tag;
    }

    public Optional<World> getWorld(MinecraftServer server){
        DimensionType type = DimensionType.getById(this.dimension);
        return type == null ? Optional.empty() : Optional.of(server.getWorld(type.getId()));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

}
