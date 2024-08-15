package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.extensions.WormholeLevel;
import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.PortalShape;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
public class PortalGroupCapability {

    public static void registerListeners(){
        NeoForge.EVENT_BUS.addListener((Consumer<LevelTickEvent.Post>)event -> tickLevelCapability(event.getLevel()));
        NeoForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerChangedDimensionEvent>)event -> onJoinWorld((ServerPlayer)event.getEntity(), event.getEntity().level()));
        NeoForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerRespawnEvent>)event -> onRespawn((ServerPlayer)event.getEntity()));
        NeoForge.EVENT_BUS.addListener((Consumer<PlayerEvent.PlayerLoggedInEvent>)event -> onJoin((ServerPlayer)event.getEntity()));
    }

    public static PortalGroupCapability get(Level level){
        return ((WormholeLevel)level).wormholeGetPortalGroupCapability();
    }

    private static void tickLevelCapability(Level level){
        get(level).tick();
    }

    private static void onJoinWorld(ServerPlayer player, Level level){
        Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(get(level).write()));
    }

    private static void onRespawn(ServerPlayer player){
        Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(get(player.level()).write()));
    }

    private static void onJoin(ServerPlayer player){
        Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(get(player.level()).write()));
    }

    private final Level level;
    private final List<PortalGroup> groups = new LinkedList<>();
    private final Map<BlockPos,PortalGroup> groupsByPosition = new HashMap<>();

    public PortalGroupCapability(Level level){
        this.level = level;
    }

    public void add(PortalShape shape){
        PortalGroup group = new PortalGroup(this.level, shape);
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
        if(!this.level.isClientSide && group != null)
            Wormhole.CHANNEL.sendToDimension(this.level, new UpdateGroupPacket(this.writeGroup(group)));
    }

    private void update(){
        Wormhole.CHANNEL.sendToDimension(this.level, new UpdateGroupsPacket(this.write()));
    }

    public PortalGroup getGroup(PortalGroupBlockEntity entity){
        return this.groupsByPosition.get(entity.getBlockPos());
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
                PortalGroup group = new PortalGroup(this.level, groupsTag.getCompound(key));
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
            PortalGroup group = new PortalGroup(this.level, tag.getCompound("group"));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }
}
