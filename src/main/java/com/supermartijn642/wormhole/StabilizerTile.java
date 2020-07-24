package com.supermartijn642.wormhole;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerTile extends PortalGroupTile {

    public StabilizerTile(){
        super(Wormhole.stabilizer_tile);
    }

    public boolean activate(PlayerEntity player, Hand hand){
        if(player.getHeldItem(hand).getItem() instanceof TargetItem){
            if(!this.world.isRemote){
                if(TargetItem.hasTarget(player.getHeldItem(hand))){
                    PortalTarget target = TargetItem.getTarget(player.getHeldItem(hand));

                    if(this.group != null && !this.group.shape.validateFrame(this.world))
                        this.group.invalidate();

                    if(this.group != null){
                        this.group.setTarget(target);
                        player.sendMessage(new TranslationTextComponent("wormhole.portal_stabilizer.success").applyTextStyle(TextFormatting.YELLOW));
                    }else{
                        PortalShape shape = PortalShape.find(this.world, this.pos);
                        if(shape == null)
                            player.sendMessage(new TranslationTextComponent("wormhole.portal_stabilizer.error").applyTextStyle(TextFormatting.RED));
                        else{
                            this.group = new PortalGroup(shape);
                            this.group.tick(this.world);
                            this.group.setTarget(target);
                            player.sendMessage(new TranslationTextComponent("wormhole.portal_stabilizer.success").applyTextStyle(TextFormatting.YELLOW));
                        }
                    }
                }else
                    player.sendMessage(new TranslationTextComponent("wormhole.target_device.error").applyTextStyle(TextFormatting.RED));
            }
            return true;
        }else if(player.isShiftKeyDown() && player.getHeldItem(hand).isEmpty()){
            if(!this.world.isRemote){
                if(this.group != null){
                    this.group.removeTarget();
                    player.sendMessage(new TranslationTextComponent("wormhole.portal_stabilizer.clear").applyTextStyle(TextFormatting.YELLOW));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setGroup(PortalGroup group){
        if(this.group != null && group == null && this.getBlockState().getBlock() instanceof StabilizerBlock && this.getBlockState().get(StabilizerBlock.ON_PROPERTY))
            this.world.setBlockState(this.pos, Wormhole.portal_stabilizer.getDefaultState().with(StabilizerBlock.ON_PROPERTY, false));
        super.setGroup(group);
    }
}
