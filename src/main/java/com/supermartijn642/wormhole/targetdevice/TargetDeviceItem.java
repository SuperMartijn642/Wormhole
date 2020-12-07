package com.supermartijn642.wormhole.targetdevice;

import com.google.common.collect.Lists;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class TargetDeviceItem extends Item {

    private final Supplier<Integer> maxTargetCount;

    public TargetDeviceItem(String registryName, Supplier<Integer> maxTargetCount){
        super(new Properties().maxStackSize(1).group(ItemGroup.SEARCH));
        this.maxTargetCount = maxTargetCount;
        this.setRegistryName(registryName);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
        if(worldIn.isRemote)
            ClientProxy.openTargetDeviceScreen(handIn, playerIn.getPosition(), Math.round(playerIn.rotationYaw / 90) * 90);
        return ActionResult.resultConsume(playerIn.getHeldItem(handIn));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        CompoundNBT tag = stack.getOrCreateTag();
        PortalTarget target = tag.contains("target") ? PortalTarget.read(tag.getCompound("target")) : null;
        IFormattableTextComponent info = target == null ? new TranslationTextComponent("wormhole.target_device.info.unset") :
            new TranslationTextComponent("wormhole.target_device.info.set", target.x, target.y, target.z);
        tooltip.add(info.mergeStyle(TextFormatting.YELLOW));
    }

    public static List<PortalTarget> getTargets(ItemStack stack){
        CompoundNBT tag = stack.getOrCreateTag();

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

        CompoundNBT tag = stack.getOrCreateTag();
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
