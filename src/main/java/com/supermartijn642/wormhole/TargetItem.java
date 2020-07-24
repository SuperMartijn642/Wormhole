package com.supermartijn642.wormhole;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
            if(playerIn.isShiftKeyDown()){
                tag.remove("target");
                playerIn.sendMessage(new TranslationTextComponent("wormhole.target_device.clear").applyTextStyle(TextFormatting.YELLOW));
            }else{
                tag.put("target", new PortalTarget(worldIn, playerIn.getPosition(), Math.round(playerIn.rotationYaw / 90) * 90).write());
                playerIn.sendMessage(new StringTextComponent(I18n.format("wormhole.target_device.set")
                    .replace("$x$", "" + playerIn.getPosition().getX())
                    .replace("$y$", "" + playerIn.getPosition().getY())
                    .replace("$z$", "" + playerIn.getPosition().getZ())
                    .replace("$dim$", worldIn.dimension.getType().getRegistryName().getPath())
                    .replace("$dir$", I18n.format("wormhole.facing." + Direction.fromAngle(playerIn.rotationYaw).getName())))
                    .applyTextStyle(TextFormatting.YELLOW));
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        CompoundNBT tag = stack.getOrCreateTag();
        PortalTarget target = tag.contains("target") ? PortalTarget.read(tag.getCompound("target")) : null;
        String info = target == null ? I18n.format("wormhole.target_device.info.unset") :
            I18n.format("wormhole.target_device.info.set")
            .replace("$x$", "" + target.x)
            .replace("$y$", "" + target.y)
            .replace("$z$", "" + target.z);
        tooltip.add(new StringTextComponent(info).applyTextStyle(TextFormatting.YELLOW));
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
