package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
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
import net.minecraftforge.fml.network.PacketDistributor;

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
                instance.read((CompoundNBT)nbt);
            }
        }, PortalGroupCapability::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World world = e.getObject();

        LazyOptional<PortalGroupCapability> capability = LazyOptional.of(() -> new PortalGroupCapability(world));
        e.addCapability(new ResourceLocation("wormhole", "portal_groups"), new ICapabilitySerializable<INBT>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CAPABILITY ? capability.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT(){
                return CAPABILITY.writeNBT(capability.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt){
                CAPABILITY.readNBT(capability.orElse(null), null, nbt);
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

    public static void tickWorldCapability(World world){
        world.getCapability(CAPABILITY).ifPresent(PortalGroupCapability::tick);
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getPlayer();
        player.world.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGroupsPacket(groups.write()))
        );
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        ServerPlayerEntity player = (ServerPlayerEntity)e.getPlayer();
        player.world.getCapability(CAPABILITY).ifPresent(groups ->
            Wormhole.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateGroupsPacket(groups.write()))
        );
    }

    private final World world;
    private final List<PortalGroup> groups = new LinkedList<>();
    private final Map<BlockPos,PortalGroup> groupsByPosition = new HashMap<>();

    public PortalGroupCapability(World world){
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
        if(!this.world.isRemote && group != null)
            Wormhole.CHANNEL.send(PacketDistributor.DIMENSION.with(this.world::func_234923_W_), new UpdateGroupPacket(this.writeGroup(group)));
    }

    private void update(){
        Wormhole.CHANNEL.send(PacketDistributor.DIMENSION.with(this.world::func_234923_W_), new UpdateGroupsPacket(this.write()));
    }

    public PortalGroup getGroup(PortalGroupTile tile){
        return this.groupsByPosition.get(tile.getPos());
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

    public void read(CompoundNBT compound){
        this.groups.clear();
        this.groupsByPosition.clear();
        CompoundNBT groupsTag = compound.getCompound("groups");
        for(String key : groupsTag.keySet()){
            PortalGroup group = new PortalGroup(this.world, groupsTag.getCompound(key));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }

    private CompoundNBT writeGroup(PortalGroup group){
        CompoundNBT tag = new CompoundNBT();
        tag.put("group", group.write());
        return tag;
    }

    public void readGroup(CompoundNBT tag){
        if(tag.contains("group")){
            PortalGroup group = new PortalGroup(this.world, tag.getCompound("group"));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }
    
}
