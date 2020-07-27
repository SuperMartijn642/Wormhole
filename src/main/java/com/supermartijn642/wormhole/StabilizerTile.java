package com.supermartijn642.wormhole;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerTile extends PortalGroupTile {

    public StabilizerTile(){
        super();
    }

    public boolean activate(EntityPlayer player, EnumHand hand){
        if(player.getHeldItem(hand).getItem() instanceof TargetItem){
            if(!this.world.isRemote){
                if(TargetItem.hasTarget(player.getHeldItem(hand))){
                    PortalTarget target = TargetItem.getTarget(player.getHeldItem(hand));

                    if(this.group != null && !this.group.shape.validateFrame(this.world))
                        this.group.invalidate();

                    if(this.group != null){
                        this.group.setTarget(target);
                        player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.success").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    }else{
                        PortalShape shape = PortalShape.find(this.world, this.pos);
                        if(shape == null)
                            player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.error").setStyle(new Style().setColor(TextFormatting.RED)));
                        else{
                            this.group = new PortalGroup(shape);
                            this.group.tick(this.world);
                            this.group.setTarget(target);
                            player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.success").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }
                    }
                }else
                    player.sendMessage(new TextComponentTranslation("wormhole.target_device.error").setStyle(new Style().setColor(TextFormatting.RED)));
            }
            return true;
        }else if(player.isSneaking() && player.getHeldItem(hand).isEmpty()){
            if(!this.world.isRemote){
                if(this.group != null){
                    this.group.removeTarget();
                    player.sendMessage(new TextComponentTranslation("wormhole.portal_stabilizer.clear").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setGroup(PortalGroup group){
        if(this.group != null && group == null && this.getBlockState().getBlock() instanceof StabilizerBlock && this.getBlockState().getValue(StabilizerBlock.ON_PROPERTY))
            this.world.setBlockState(this.pos, Wormhole.portal_stabilizer.getDefaultState().withProperty(StabilizerBlock.ON_PROPERTY, false));
        super.setGroup(group);
    }

    private IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }
}
