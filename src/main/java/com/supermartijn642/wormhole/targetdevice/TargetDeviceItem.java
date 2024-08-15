package com.supermartijn642.wormhole.targetdevice;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeClient;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class TargetDeviceItem extends BaseItem {

    public static DataComponentType<List<PortalTarget>> TARGETS = DataComponentType.<List<PortalTarget>>builder()
        .persistent(RecordCodecBuilder.<PortalTarget>create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(t -> t.dimension),
            Codec.INT.fieldOf("x").forGetter(t -> t.x),
            Codec.INT.fieldOf("y").forGetter(t -> t.y),
            Codec.INT.fieldOf("z").forGetter(t -> t.z),
            Codec.FLOAT.fieldOf("yaw").forGetter(t -> t.yaw),
            Codec.STRING.fieldOf("name").forGetter(t -> t.name),
            DyeColor.CODEC.optionalFieldOf("color").forGetter(t -> Optional.ofNullable(t.color))
        ).apply(instance, (dim, x, y, z, yaw, name, color) -> new PortalTarget(dim, x, y, z, yaw, name, color.orElse(null)))).listOf())
        .networkSynchronized(StreamCodec.<RegistryFriendlyByteBuf,PortalTarget,ResourceKey<Level>,BlockPos,Float,String,Optional<DyeColor>>composite(
            ResourceKey.streamCodec(Registries.DIMENSION), t -> t.dimension,
            BlockPos.STREAM_CODEC, t -> new BlockPos(t.x, t.y, t.z),
            ByteBufCodecs.FLOAT, t -> t.yaw,
            ByteBufCodecs.STRING_UTF8, t -> t.name,
            ByteBufCodecs.optional(DyeColor.STREAM_CODEC), t -> Optional.ofNullable(t.color),
            (dim, pos, yaw, name, color) -> new PortalTarget(dim, pos.getX(), pos.getY(), pos.getZ(), yaw, name, color.orElse(null))
        ).apply(ByteBufCodecs.list()))
        .build();

    private final Supplier<Integer> maxTargetCount;

    public TargetDeviceItem(Supplier<Integer> maxTargetCount){
        super(ItemProperties.create().maxStackSize(1).group(Wormhole.ITEM_GROUP));
        this.maxTargetCount = maxTargetCount;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        if(level.isClientSide)
            WormholeClient.openTargetDeviceScreen(hand, player.blockPosition(), Math.round(player.getYRot() / 90) * 90);
        return ItemUseResult.consume(player.getItemInHand(hand));
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.target_device.info").color(ChatFormatting.AQUA).get());

        List<PortalTarget> targets = getTargets(stack);
        int capacity = getMaxTargetCount(stack);
        info.accept(TextComponents.translation("wormhole.target_device.info.targets", targets.size(), capacity).color(ChatFormatting.YELLOW).get());
    }

    public static List<PortalTarget> getTargets(ItemStack stack){
        //noinspection DataFlowIssue
        return stack.has(TARGETS) ? Collections.unmodifiableList(stack.get(TARGETS)) : Collections.emptyList();
    }

    public static void setTargets(ItemStack stack, List<PortalTarget> targets){
        if(targets == null || targets.isEmpty())
            stack.remove(TARGETS);
        else
            stack.set(TARGETS, targets);
    }

    public static void addTarget(ItemStack stack, PortalTarget target){
        List<PortalTarget> list = new ArrayList<>(getTargets(stack));
        list.add(target);
        setTargets(stack, list);
    }

    public static void removeTarget(ItemStack stack, int index){
        if(index < 0)
            return;
        List<PortalTarget> list = getTargets(stack);
        if(index >= list.size())
            return;
        list = new ArrayList<>(list);
        list.remove(index);
        setTargets(stack, list);
    }

    public static void moveTarget(ItemStack stack, int index, boolean up){
        if(index < 0)
            return;
        List<PortalTarget> list = getTargets(stack);
        if(index >= list.size())
            return;
        if(up ? index == 0 : index == list.size() - 1)
            return;
        list = new ArrayList<>(list);
        PortalTarget target = list.remove(index);
        list.add(index + (up ? -1 : 1), target);
        setTargets(stack, list);
    }

    public static void changeTargetName(ItemStack stack, int index, String name){
        if(index < 0)
            return;
        List<PortalTarget> list = getTargets(stack);
        if(index >= list.size())
            return;
        list.get(index).name = name;
        setTargets(stack, list);
    }

    public static int getMaxTargetCount(ItemStack stack){
        return stack.getItem() instanceof TargetDeviceItem ? ((TargetDeviceItem)stack.getItem()).maxTargetCount.get() : -1;
    }
}
