package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

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
                for(BlockPos pos : this.getGroup().shape.area){
                    BlockState state = this.world.getBlockState(pos);
                    if(state.getBlock() == Wormhole.portal && state.get(PortalBlock.COLOR_PROPERTY) != color)
                        this.world.setBlockState(pos, state.with(PortalBlock.COLOR_PROPERTY, color));
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
