package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.util.Hand;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    public PortalTile(){
        super(Wormhole.portal_tile);
    }

    public void teleport(Entity entity){
        if(this.hasGroup())
            this.getGroup().teleport(entity);
    }

    public boolean activate(PlayerEntity player, Hand hand){
        if(player.getHeldItem(hand).getItem() instanceof DyeItem){
            DyeColor color = ((DyeItem)player.getHeldItem(hand).getItem()).getDyeColor();
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
