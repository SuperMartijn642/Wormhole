package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.PortalShape;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortalGroupCapability {

    @CapabilityInject(PortalGroupCapability.class)
    public static Capability<PortalGroupCapability> CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(PortalGroupCapability.class, new Capability.IStorage<PortalGroupCapability>() {
            public CompoundNBT writeNBT(Capability<PortalGroupCapability> capability, PortalGroupCapability instance, Direction side){
                return instance.write();
            }

            public void readNBT(Capability<PortalGroupCapability> capability, PortalGroupCapability instance, Direction side, INBT nbt){
                instance.read(nbt);
            }
        }, () -> new PortalGroupCapability(null));
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World level = e.getObject();

        LazyOptional<PortalGroupCapability> capability = LazyOptional.of(() -> new PortalGroupCapability(level));
        e.addCapability(new ResourceLocation("wormhole", "portal_groups"), new ICapabilitySerializable<INBT>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CAPABILITY ? capability.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT(){
                return capability.map(PortalGroupCapability::write).orElse(null);
            }

            @Override
            public void deserializeNBT(INBT nbt){
                capability.ifPresent(portalGroupCapability -> portalGroupCapability.read(nbt));
            }
        });
        e.addListener(capability::invalidate);
    }


    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END)
            return;

        tickLevelCapability(e.world);
    }

    public static void tickLevelCapability(World level){
        level.getCapability(CAPABILITY).ifPresent(PortalGroupCapability::tick);
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getEntity();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getEntity();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getEntity();
        player.level.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.sendToPlayer(player, new UpdateGroupsPacket(groups.write()))
        );
    }

    private final World level;
    private final List<PortalGroup> groups = new LinkedList<>();
    private final Map<BlockPos,PortalGroup> groupsByPosition = new HashMap<>();

    public PortalGroupCapability(World level){
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

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT groupsTag = new CompoundNBT();
        for(int i = 0; i < this.groups.size(); i++)
            groupsTag.put("groups" + i, this.groups.get(i).write());
        compound.put("groups", groupsTag);
        return compound;
    }

    public void read(INBT tag){
        if(tag instanceof CompoundNBT){
            CompoundNBT compound = (CompoundNBT)tag;
            this.groups.clear();
            this.groupsByPosition.clear();
            CompoundNBT groupsTag = compound.getCompound("groups");
            for(String key : groupsTag.getAllKeys()){
                PortalGroup group = new PortalGroup(this.level, groupsTag.getCompound(key));
                this.groups.add(group);
                group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
                group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
            }
        }
    }

    private CompoundNBT writeGroup(PortalGroup group){
        CompoundNBT tag = new CompoundNBT();
        tag.put("group", group.write());
        return tag;
    }

    public void readGroup(CompoundNBT tag){
        if(tag.contains("group")){
            PortalGroup group = new PortalGroup(this.level, tag.getCompound("group"));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }
}
