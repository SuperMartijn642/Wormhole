package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    public void teleport(Entity entity){
        if(this.hasGroup())
            this.getGroup().teleport(entity);
    }

    public boolean activate(EntityPlayer player, EnumHand hand){ // TODO change the active target's color instead changes the color on the portals directly
        if(player.getHeldItem(hand).getItem() instanceof ItemDye){
            EnumDyeColor color = EnumDyeColor.byDyeDamage(player.getHeldItem(hand).getMetadata());
            if(this.hasGroup() && this.getGroup().getActiveTarget() != null){
                for(BlockPos pos : this.getGroup().shape.area){
                    IBlockState state = this.world.getBlockState(pos);
                    if(state.getBlock() == this.getBlockType() && state.getValue(PortalBlock.COLOR_PROPERTY) != color)
                        this.world.setBlockState(pos, state.withProperty(PortalBlock.COLOR_PROPERTY, color));
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
