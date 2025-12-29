package com.supermartijn642.wormhole.portal;

import com.supermartijn642.core.TextComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public DyeColor color;
    public Component dimensionDisplayName;

    public PortalTarget(ResourceKey<Level> dimension, int x, int y, int z, float yaw, String name, DyeColor color){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;
        this.color = color;
        this.dimensionDisplayName = TextComponents.dimension(dimension).get();
    }

    public PortalTarget(Level level, BlockPos pos, float yaw, String name){
        this(level.dimension(), pos.getX(), pos.getY(), pos.getZ(), yaw, name, null);
    }

    public PortalTarget(ValueInput input){
        //noinspection OptionalGetWithoutIsPresent
        this(
            ResourceKey.create(Registries.DIMENSION, input.getString("dimension").map(Identifier::parse).get()),
            input.getIntOr("x", 0), input.getIntOr("y", 0), input.getIntOr("z", 0),
            input.getFloatOr("yaw", 0),
            input.getString("name").orElse("Target Destination"),
            input.getInt("color").map(DyeColor::byId).orElse(null)
        );
    }

    public static PortalTarget read(ValueInput input){
        return new PortalTarget(input);
    }

    public void write(ValueOutput output){
        output.putString("dimension", this.dimension.identifier().toString());
        output.putInt("x", this.x);
        output.putInt("y", this.y);
        output.putInt("z", this.z);
        output.putFloat("yaw", this.yaw);
        output.putString("name", this.name);
        if(this.color != null)
            output.putInt("color", this.color.getId());
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
