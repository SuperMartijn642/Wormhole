package com.supermartijn642.wormhole;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_frame), 0, new ModelResourceLocation(Wormhole.portal_frame.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_x), 0, new ModelResourceLocation(Wormhole.portal_x.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_y), 0, new ModelResourceLocation(Wormhole.portal_y.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_z), 0, new ModelResourceLocation(Wormhole.portal_z.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wormhole.portal_stabilizer), 0, new ModelResourceLocation(Wormhole.portal_stabilizer.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Wormhole.target_device, 0, new ModelResourceLocation(Wormhole.target_device.getRegistryName(), "inventory"));
    }

}
