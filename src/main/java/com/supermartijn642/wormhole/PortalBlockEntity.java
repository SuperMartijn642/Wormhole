package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupBlockEntity;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumHand;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalBlockEntity extends PortalGroupBlockEntity {

    public PortalBlockEntity(){
        super(Wormhole.portal_tile);
    }

    public void teleport(Entity entity){
        if(this.hasGroup())
            this.getGroup().teleport(entity);
    }

    public boolean activate(EntityPlayer player, EnumHand hand){
        if(player.getHeldItem(hand).getItem() instanceof ItemDye){
            EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(hand).getMetadata());
            if(this.hasGroup() && this.getGroup().getActiveTarget() != null){
                PortalGroup group = this.getGroup();
                PortalTarget target = group.getTarget(group.getActiveTargetIndex());
                if(target != null){
                    target.color = color;
                    group.setTarget(group.getActiveTargetIndex(), target);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBreak(){
        if(this.hasGroup())
            this.getGroup().deactivate();
    }
}
