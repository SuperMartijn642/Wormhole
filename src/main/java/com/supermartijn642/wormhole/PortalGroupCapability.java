package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortalGroupCapability {

    public static Capability<PortalGroupCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent e){
        e.register(PortalGroupCapability.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Level> e){
        Level world = e.getObject();

        LazyOptional<PortalGroupCapability> capability = LazyOptional.of(() -> new PortalGroupCapability(world));
        e.addCapability(new ResourceLocation("wormhole", "portal_groups"), new ICapabilitySerializable<Tag>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CAPABILITY ? capability.cast() : LazyOptional.empty();
            }

            @Override
            public Tag serializeNBT(){
                return capability.map(PortalGroupCapability::write).orElse(null);
            }

            @Override
            public void deserializeNBT(Tag nbt){
                capability.ifPresent(portalGroupCapability -> portalGroupCapability.read(nbt));
            }
        });
        e.addListener(capability::invalidate);
    }


    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END)
            return;

        tickWorldCapability(e.world);
    }

    public static void tickWorldCapability(Level world){
        world.getCapability(CAPABILITY).ifPresent(PortalGroupCapability::tick);
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        ServerPlayer player = (ServerPlayer)e.getPlayer();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGroupsPacket(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e){
        ServerPlayer player = (ServerPlayer)e.getPlayer();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGroupsPacket(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        ServerPlayer player = (ServerPlayer)e.getPlayer();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGroupsPacket(groups.write()))
        );
    }

    private final Level world;
    private final List<PortalGroup> groups = new LinkedList<>();
    private final Map<BlockPos,PortalGroup> groupsByPosition = new HashMap<>();

    public PortalGroupCapability(Level world){
        this.world = world;
    }

    public PortalGroupCapability(){
        this.world = null;
    }

    public void add(PortalShape shape){
        PortalGroup group = new PortalGroup(this.world, shape);
        this.groups.add(group);
        group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
        group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        this.update();
    }

    public void remove(PortalGroup group){
        this.groups.remove(group);
        group.shape.frame.forEach(this.groupsByPosition::remove);
        group.shape.area.forEach(this.groupsByPosition::remove);
        this.update();
    }

    public void tick(){
        for(PortalGroup group : this.groupsByPosition.values())
            group.canTick = true;
    }

    public void updateGroup(PortalGroup group){
        if(!this.world.isClientSide && group != null)
            Wormhole.CHANNEL.send(PacketDistributor.DIMENSION.with(this.world::dimension), new UpdateGroupPacket(this.writeGroup(group)));
    }

    private void update(){
        Wormhole.CHANNEL.send(PacketDistributor.DIMENSION.with(this.world::dimension), new UpdateGroupsPacket(this.write()));
    }

    public PortalGroup getGroup(PortalGroupTile tile){
        return this.groupsByPosition.get(tile.getBlockPos());
    }

    public PortalGroup getGroup(BlockPos pos){
        return this.groupsByPosition.get(pos);
    }

    public Collection<PortalGroup> getGroups(){
        return this.groupsByPosition.values();
    }

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        CompoundTag groupsTag = new CompoundTag();
        for(int i = 0; i < this.groups.size(); i++)
            groupsTag.put("groups" + i, this.groups.get(i).write());
        compound.put("groups", groupsTag);
        return compound;
    }

    public void read(Tag tag){
        if(tag instanceof CompoundTag compound){
            this.groups.clear();
            this.groupsByPosition.clear();
            CompoundTag groupsTag = compound.getCompound("groups");
            for(String key : groupsTag.getAllKeys()){
                PortalGroup group = new PortalGroup(this.world, groupsTag.getCompound(key));
                this.groups.add(group);
                group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
                group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
            }
        }
    }

    private CompoundTag writeGroup(PortalGroup group){
        CompoundTag tag = new CompoundTag();
        tag.put("group", group.write());
        return tag;
    }

    public void readGroup(CompoundTag tag){
        if(tag.contains("group")){
            PortalGroup group = new PortalGroup(this.world, tag.getCompound("group"));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }

}
