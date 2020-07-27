package com.supermartijn642.wormhole;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class TargetItem extends Item {
    public TargetItem(){
        super();
        this.setRegistryName("target_device");
        this.setUnlocalizedName(Wormhole.MODID + ".target_device");

        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn){
        if(!worldIn.isRemote){
            ItemStack stack = playerIn.getHeldItem(handIn);
            NBTTagCompound tag = stack.getTagCompound();
            if(tag == null)
                tag = new NBTTagCompound();
            if(playerIn.isSneaking()){
                tag.removeTag("target");
                playerIn.sendMessage(new TextComponentTranslation("wormhole.target_device.clear").setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }else{
                tag.setTag("target", new PortalTarget(worldIn, playerIn.getPosition(), Math.round(playerIn.rotationYaw / 90) * 90).write());
                playerIn.sendMessage(new TextComponentString(I18n.format("wormhole.target_device.set")
                    .replace("$x$", "" + playerIn.getPosition().getX())
                    .replace("$y$", "" + playerIn.getPosition().getY())
                    .replace("$z$", "" + playerIn.getPosition().getZ())
                    .replace("$dim$", worldIn.provider.getDimensionType().getName())
                    .replace("$dir$", I18n.format("wormhole.facing." + EnumFacing.fromAngle(playerIn.rotationYaw).getName())))
                    .setStyle(new Style().setColor(TextFormatting.YELLOW))
                );
            }
            stack.setTagCompound(tag);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        NBTTagCompound tag = stack.getTagCompound();
        PortalTarget target = tag != null && tag.hasKey("target") ? PortalTarget.read(tag.getCompoundTag("target")) : null;
        String info = target == null ? I18n.format("wormhole.target_device.info.unset") :
            I18n.format("wormhole.target_device.info.set")
                .replace("$x$", "" + target.x)
                .replace("$y$", "" + target.y)
                .replace("$z$", "" + target.z);
        tooltip.add(TextFormatting.YELLOW + info);
    }

    public static boolean hasTarget(ItemStack stack){
        return stack.getItem() instanceof TargetItem && stack.hasTagCompound() && stack.getTagCompound().hasKey("target");
    }

    public static PortalTarget getTarget(ItemStack stack){
        if(!hasTarget(stack))
            return null;
        return PortalTarget.read(stack.getTagCompound().getCompoundTag("target"));
    }
}
