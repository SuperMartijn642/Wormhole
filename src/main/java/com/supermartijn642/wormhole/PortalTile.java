package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalGroupTile;
import com.supermartijn642.wormhole.portal.PortalTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    public PortalTile(BlockPos pos, BlockState state){
        super(Wormhole.portal_tile, pos, state);
    }

    public void teleport(Entity entity){
        if(this.hasGroup())
            this.getGroup().teleport(entity);
    }

    public boolean activate(Player player, InteractionHand hand){
        if(player.getItemInHand(hand).getItem() instanceof DyeItem){
            DyeColor color = ((DyeItem)player.getItemInHand(hand).getItem()).getDyeColor();
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
