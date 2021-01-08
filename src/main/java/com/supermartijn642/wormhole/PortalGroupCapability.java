package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.packet.UpdateGroupPacket;
import com.supermartijn642.wormhole.packet.UpdateGroupsPacket;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalShape;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created 11/9/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class PortalGroupCapability {

    @CapabilityInject(PortalGroupCapability.class)
    public static Capability<PortalGroupCapability> CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(PortalGroupCapability.class, new Capability.IStorage<PortalGroupCapability>() {
            public NBTTagCompound writeNBT(Capability<PortalGroupCapability> capability, PortalGroupCapability instance, EnumFacing side){
                return instance.write();
            }

            public void readNBT(Capability<PortalGroupCapability> capability, PortalGroupCapability instance, EnumFacing side, NBTBase nbt){
                instance.read((NBTTagCompound)nbt);
            }
        }, PortalGroupCapability::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World world = e.getObject();

        PortalGroupCapability capability = new PortalGroupCapability(world);
        e.addCapability(new ResourceLocation("wormhole", "portal_groups"), new ICapabilitySerializable<NBTBase>() {
            @Nonnull
            @Override
            public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side){
                return cap == CAPABILITY ? CAPABILITY.cast(capability) : null;
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing){
                return capability == CAPABILITY;
            }

            @Override
            public NBTBase serializeNBT(){
                return CAPABILITY.writeNBT(capability, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt){
                CAPABILITY.readNBT(capability, null, nbt);
            }
        });
    }


    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END)
            return;

        tickWorldCapability(e.world);
    }

    public static void tickWorldCapability(World world){
        PortalGroupCapability groups = world.getCapability(CAPABILITY, null);
        if(groups != null)
            groups.tick();
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerChangedDimensionEvent e){
        EntityPlayerMP player = (EntityPlayerMP)e.player;
        PortalGroupCapability groups = player.world.getCapability(CAPABILITY, null);
        if(groups != null)
            Wormhole.channel.sendTo(new UpdateGroupsPacket(groups.write()), player);
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent e){
        EntityPlayerMP player = (EntityPlayerMP)e.player;
        PortalGroupCapability groups = player.world.getCapability(CAPABILITY, null);
        if(groups != null)
            Wormhole.channel.sendTo(new UpdateGroupsPacket(groups.write()), player);
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
            Wormhole.channel.sendToDimension(new UpdateGroupPacket(this.writeGroup(group)), this.world.provider.getDimensionType().getId());
    }

    private void update(){
        Wormhole.channel.sendToDimension(new UpdateGroupsPacket(this.write()), this.world.provider.getDimensionType().getId());
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

    public NBTTagCompound write(){
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound groupsTag = new NBTTagCompound();
        for(int i = 0; i < this.groups.size(); i++)
            groupsTag.setTag("groups" + i, this.groups.get(i).write());
        compound.setTag("groups", groupsTag);
        return compound;
    }

    public void read(NBTTagCompound compound){
        this.groups.clear();
        this.groupsByPosition.clear();
        NBTTagCompound groupsTag = compound.getCompoundTag("groups");
        for(String key : groupsTag.getKeySet()){
            PortalGroup group = new PortalGroup(this.world, groupsTag.getCompoundTag(key));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }

    private NBTTagCompound writeGroup(PortalGroup group){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("group", group.write());
        return tag;
    }

    public void readGroup(NBTTagCompound tag){
        if(tag.hasKey("group")){
            PortalGroup group = new PortalGroup(this.world, tag.getCompoundTag("group"));
            this.groups.add(group);
            group.shape.frame.forEach(pos -> this.groupsByPosition.put(pos, group));
            group.shape.area.forEach(pos -> this.groupsByPosition.put(pos, group));
        }
    }

}
