package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.TextComponents;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTarget {

    public static final int MAX_NAME_LENGTH = 10;

    public final DimensionType dimension;
    public final int x, y, z;
    public final float yaw;

    public String name;
    public DyeColor color = null;
    public ITextComponent dimensionDisplayName;

    public PortalTarget(DimensionType dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;
        this.dimensionDisplayName = TextComponents.dimension(dimension).get();
    }

    public PortalTarget(World level, BlockPos pos, float yaw, String name){
        this(level.getDimension().getType(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(CompoundNBT tag){
        this(DimensionType.getById(tag.getInt("dimension")), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getFloat("yaw"), tag.contains("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : null;
    }

    public static PortalTarget read(CompoundNBT tag){
        return new PortalTarget(tag);
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("dimension", this.dimension.getId());
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        tag.putFloat("yaw", this.yaw);
        tag.putString("name", this.name);
        if(this.color != null)
            tag.putInt("color", this.color.getId());
        return tag;
    }

    public Optional<World> getLevel(MinecraftServer server){
        return Optional.ofNullable(server.getLevel(this.dimension));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

    public Vec3d getCenteredPos(){
        return new Vec3d(this.x + 0.5, this.y + 0.2, this.z + 0.5);
    }

    public ITextComponent getDimensionDisplayName(){
        return this.dimensionDisplayName;
    }
}
