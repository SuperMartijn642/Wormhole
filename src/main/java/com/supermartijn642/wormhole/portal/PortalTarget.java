package com.supermartijn642.wormhole.portal;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    public String dimensionDisplayName;

    public PortalTarget(int dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;

        DimensionType type = DimensionType.getById(this.dimension);
        String dimensionString = type == null ? "" : type.getName();
        String dimensionName = dimensionString.substring(Math.min(dimensionString.length() - 1, Math.max(0, dimensionString.indexOf(':') + 1))).toLowerCase();
        dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        for(int i = 0; i < dimensionName.length() - 1; i++)
            if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
        this.dimensionDisplayName = dimensionName;
    }

    public PortalTarget(World world, BlockPos pos, float yaw, String name){
        this(world.provider.getDimensionType().getId(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(NBTTagCompound tag){
        this(tag.getInteger("dimension"), tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getFloat("yaw"), tag.hasKey("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.hasKey("color") ? EnumDyeColor.byDyeDamage(tag.getInteger("color")) : null;

        DimensionType type = DimensionType.getById(this.dimension);
        String dimension = type == null ? "" : type.getName();
        String dimensionName = dimension.substring(Math.min(dimension.length() - 1, Math.max(0, dimension.indexOf(':') + 1))).toLowerCase();
        dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        for(int i = 0; i < dimensionName.length() - 1; i++)
            if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
        this.dimensionDisplayName = dimensionName;
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

    public Vec3d getCenteredPos(){
        return new Vec3d(this.x + 0.5, this.y + 0.2, this.z + 0.5);
    }

    public String getDimensionDisplayName(){
        return this.dimensionDisplayName;
    }
}
