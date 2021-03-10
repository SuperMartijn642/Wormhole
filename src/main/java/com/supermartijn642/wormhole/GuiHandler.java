package com.supermartijn642.wormhole;

import com.supermartijn642.wormhole.generator.CoalGeneratorContainer;
import com.supermartijn642.wormhole.generator.CoalGeneratorScreen;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * Created 1/7/2021 by SuperMartijn642
 */
public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if(block == Wormhole.coal_generator)
            return new CoalGeneratorContainer(player, pos);
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if(block == Wormhole.coal_generator)
            return new CoalGeneratorScreen(new CoalGeneratorContainer(player, pos), player);
        return null;
    }
}
