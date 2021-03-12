package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.portal.PortalGroupBlock;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class StabilizerBlock extends PortalGroupBlock {

    public static final PropertyBool ON_PROPERTY = PropertyBool.create("on");

    public StabilizerBlock(){
        super("portal_stabilizer", StabilizerTile::new);
        this.setDefaultState(this.getDefaultState().withProperty(ON_PROPERTY, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof StabilizerTile)
            return ((StabilizerTile)tile).activate(player);
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ON_PROPERTY);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ON_PROPERTY) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ON_PROPERTY, meta == 1);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced){
        tooltip.add(new TextComponentTranslation("wormhole.portal_stabilizer.info").setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());

        NBTTagCompound tag = (stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData")) ? stack.getTagCompound().getCompoundTag("tileData") : null;

        int targets = tag == null || tag.hasNoTags() || !tag.hasKey("targetCount") ? 0 : tag.getInteger("targetCount");
        int targetCapacity = WormholeConfig.stabilizerTargetCapacity.get();

        if(targetCapacity > 0)
            tooltip.add(new TextComponentTranslation("wormhole.portal_stabilizer.info.targets", targets, targetCapacity).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());

        int energy = tag == null || tag.hasNoTags() || !tag.hasKey("energy") ? 0 : tag.getInteger("energy");
        int energyCapacity = WormholeConfig.stabilizerEnergyCapacity.get();

        if(energyCapacity > 0)
            tooltip.add(new TextComponentString(EnergyFormat.formatCapacity(energy, energyCapacity)).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
    }
}
