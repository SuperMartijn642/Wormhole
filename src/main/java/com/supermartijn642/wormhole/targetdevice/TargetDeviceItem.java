package com.supermartijn642.wormhole.targetdevice;

import com.google.common.collect.Lists;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeClient;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class TargetDeviceItem extends BaseItem {

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
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("wormhole.target_device.info").color(ChatFormatting.AQUA).get());

        List<PortalTarget> targets = getTargets(stack);
        int capacity = getMaxTargetCount(stack);
        info.accept(TextComponents.translation("wormhole.target_device.info.targets", targets.size(), capacity).color(ChatFormatting.YELLOW).get());
    }

    public static List<PortalTarget> getTargets(ItemStack stack){
        CompoundTag tag = stack.getOrCreateTag();

        if(tag.contains("target")){
            stack.setTag(null);
            setTargets(stack, Lists.newArrayList(PortalTarget.read(tag.getCompound("target"))));
            tag = stack.getOrCreateTag();
        }

        if(!tag.contains("targetCount"))
            return new LinkedList<>();

        int count = tag.getInt("targetCount");
        List<PortalTarget> targets = new ArrayList<>(count);
        for(int i = 0; i < count; i++)
            targets.add(new PortalTarget(tag.getCompound("target" + i)));
        return targets;
    }

    public static void setTargets(ItemStack stack, List<PortalTarget> targets){
        if(targets == null || targets.size() == 0)
            stack.setTag(null);

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("targetCount", targets.size());
        for(int i = 0; i < targets.size(); i++)
            tag.put("target" + i, targets.get(i).write());
    }

    public static void addTarget(ItemStack stack, PortalTarget target){
        List<PortalTarget> list = getTargets(stack);
        list.add(target);
        setTargets(stack, list);
    }

    public static void removeTarget(ItemStack stack, int index){
        if(index < 0)
            return;
        List<PortalTarget> list = getTargets(stack);
        if(index >= list.size())
            return;
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
