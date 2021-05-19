package com.supermartijn642.wormhole.portal;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

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
    public DyeColor color = null;
    public String dimensionDisplayName;

    public PortalTarget(int dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;

        DimensionType type = DimensionType.getById(this.dimension);
        String dimensionString = type == null ? "" : type.getRegistryName().toString();
        String dimensionName = dimensionString.substring(Math.min(dimensionString.length() - 1, Math.max(0, dimensionString.indexOf(':') + 1))).toLowerCase();
        dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        for(int i = 0; i < dimensionName.length() - 1; i++)
            if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
        this.dimensionDisplayName = dimensionName;
    }

    public PortalTarget(World world, BlockPos pos, float yaw, String name){
        this(world.getDimension().getType().getId(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(CompoundNBT tag){
        this(tag.getInt("dimension"), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getFloat("yaw"), tag.contains("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : null;

        DimensionType type = DimensionType.getById(this.dimension);
        String dimension = type == null ? "" : type.getRegistryName().toString();
        String dimensionName = dimension.substring(Math.min(dimension.length() - 1, Math.max(0, dimension.indexOf(':') + 1))).toLowerCase();
        dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        for(int i = 0; i < dimensionName.length() - 1; i++)
            if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
        this.dimensionDisplayName = dimensionName;
    }

    public static PortalTarget read(CompoundNBT tag){
        return new PortalTarget(tag);
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("dimension", this.dimension);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        tag.putFloat("yaw", this.yaw);
        tag.putString("name", this.name);
        if(this.color != null)
            tag.putInt("color", this.color.getId());
        return tag;
    }

    public Optional<World> getWorld(MinecraftServer server){
        DimensionType type = DimensionType.getById(this.dimension);
        return type == null ? Optional.empty() : Optional.of(server.getWorld(type));
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
