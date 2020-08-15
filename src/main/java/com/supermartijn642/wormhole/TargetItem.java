package com.supermartijn642.wormhole;

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
import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class TargetItem extends Item {
    public TargetItem(){
        super(new Properties().maxStackSize(1).group(ItemGroup.SEARCH));
        this.setRegistryName("target_device");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
        if(!worldIn.isRemote){
            ItemStack stack = playerIn.getHeldItem(handIn);
            CompoundNBT tag = stack.getOrCreateTag();
            if(playerIn.isSneaking()){
                tag.remove("target");
                playerIn.sendMessage(new TranslationTextComponent("wormhole.target_device.clear").mergeStyle(TextFormatting.YELLOW), playerIn.getUniqueID());
            }else{
                tag.put("target", new PortalTarget(worldIn, playerIn.getPosition(), Math.round(playerIn.rotationYaw / 90) * 90).write());
                playerIn.sendMessage(new TranslationTextComponent("wormhole.target_device.set",
                    playerIn.getPosition().getX(),
                    playerIn.getPosition().getY(),
                    playerIn.getPosition().getZ())
                    .mergeStyle(TextFormatting.YELLOW), playerIn.getUniqueID());
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        CompoundNBT tag = stack.getOrCreateTag();
        PortalTarget target = tag.contains("target") ? PortalTarget.read(tag.getCompound("target")) : null;
        IFormattableTextComponent info = target == null ? new TranslationTextComponent("wormhole.target_device.info.unset") :
            new TranslationTextComponent("wormhole.target_device.info.set", target.x, target.y, target.z);
        tooltip.add(info.mergeStyle(TextFormatting.YELLOW));
    }

    public static boolean hasTarget(ItemStack stack){
        return stack.getItem() instanceof TargetItem && stack.getOrCreateTag().contains("target");
    }

    public static PortalTarget getTarget(ItemStack stack){
        if(!hasTarget(stack))
            return null;
        return PortalTarget.read(stack.getOrCreateTag().getCompound("target"));
    }
}
