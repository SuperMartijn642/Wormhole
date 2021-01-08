package com.supermartijn642.wormhole;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class WormholeConfig {

    //  -- power --
    // idle = idlePowerDrain + portal size * sizePowerDrain
    // teleport = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain + dimensionPowerDrain

    private static final String FILE_NAME = "wormhole.cfg";

    public static Configuration instance;

    public static boolean requireCorners;
    public static int maxPortalSize;
    public static boolean requireDimensionalCore; // TODO change 'dimensional core' to the actual name

    public static int basicDeviceTargetCount;
    public static int advancedDeviceTargetCount;

    public static boolean requirePower;
    public static int idlePowerDrain;
    public static float sizePowerDrain;
    public static int travelPowerDrain;
    public static float distancePowerDrain;
    public static int dimensionPowerDrain;

    public static int stabilizerEnergyCapacity;
    public static int basicEnergyCellCapacity;
    public static int advancedEnergyCellCapacity;

    public static int stabilizerTargetCapacity;
    public static int basicTargetCellCapacity;
    public static int advancedTargetCellCapacity;

    public static int coalGeneratorPower;
    public static int coalGeneratorCapacity;
    public static int coalGeneratorRange;

    public static void init(File dir){
        instance = new Configuration(new File(dir, FILE_NAME));
        instance.load();

        // General
        requireCorners = instance.getBoolean("requireCorners", "General", false, "If true, a portal will require frame blocks at its corners. Previously build portals won't be affected.");
        maxPortalSize = instance.getInt("maxPortalSize", "General", 400, 1, 2000, "How big can the area inside a portal be? Higher numbers can impact performance when activating a portal. Previously build portals won't be affected.");
        requireDimensionalCore = instance.getBoolean("requireDimensionalCore", "General", true, "Does a portal require a dimensional core for interdimensional travel? (WIP)"); // TODO remove WIP

        // Target Devices
        basicDeviceTargetCount = instance.getInt("basicDeviceTargetCount", "Target Devices", 1, 1, 10, "The maximum number of targets that can be stored in the basic target definition device.");
        advancedDeviceTargetCount = instance.getInt("advancedDeviceTargetCount", "Target Devices", 5, 1, 10, "The maximum number of targets that can be stored in the advanced target definition device.");

        // Power Consumption
        instance.addCustomCategoryComment("Power Consumption", "Power consumption will be calculated as follows:\n" +
            "Idle power drain = idlePowerDrain + portal size * sizePowerDrain\n" +
            "Interdimensional teleport power drain = travelPowerDrain + dimensionPowerDrain\n" +
            "Same dimension teleport power drain = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain");
        requirePower = instance.getBoolean("requirePower", "Power Consumption", true, "If false, a portal will not require power.");
        idlePowerDrain = instance.getInt("idlePowerDrain", "Power Consumption", 10, 0, 1000000, "How much power will a portal drain per tick when idle?");
        sizePowerDrain = instance.getFloat("sizePowerDrain", "Power Consumption", 0.1f, 0, 1000000, "How much idle power does the portal drain per block of its size?");
        travelPowerDrain = instance.getInt("travelPowerDrain", "Power Consumption", 100, 0, 1000000, "How much power will be drained when an entity is teleported?");
        distancePowerDrain = instance.getFloat("distancePowerDrain", "Power Consumption", 0.5f, 0, 1000000, "How much energy is drained when an entity is teleported multiplied by the distance?");
        dimensionPowerDrain = instance.getInt("dimensionPowerDrain", "Power Consumption", 400, 0, 1000000, "How much energy is drained when an entity is teleported to another dimension?");

        // Energy Cells
        stabilizerEnergyCapacity = instance.getInt("stabilizerEnergyCapacity", "Power Consumption", 1000, 1, 1000000, "The amount of energy the portal stabilizer can store.");
        basicEnergyCellCapacity = instance.getInt("basicEnergyCellCapacity", "Power Consumption", 2000, 1, 1000000, "The amount of energy the basic energy cell can store.");
        advancedEnergyCellCapacity = instance.getInt("advancedEnergyCellCapacity", "Power Consumption", 10000, 1, 1000000, "The amount of energy the advanced energy cell can store.");

        // Target Cells
        stabilizerTargetCapacity = instance.getInt("stabilizerTargetCapacity", "Power Consumption", 1, 1, 20, "The number of targets the portal stabilizer can store.");
        basicTargetCellCapacity = instance.getInt("basicTargetCellCapacity", "Power Consumption", 4, 1, 20, "The number of targets the basic target cell can store.");
        advancedTargetCellCapacity = instance.getInt("advancedTargetCellCapacity", "Power Consumption", 8, 1, 20, "The number of targets the advanced target cell can store.");

        // Generators
        coalGeneratorPower = instance.getInt("coalGeneratorPower", "Generators", 10, 1, 1000000, "How much energy does the coal generator generate per tick?");
        coalGeneratorCapacity = instance.getInt("coalGeneratorCapacity", "Generators", 5000, 1, 1000000, "How much energy can the coal generator store?");
        coalGeneratorRange = instance.getInt("coalGeneratorRange", "Generators", 2, 1, 4, "In what area (coalGeneratorRange^3) should the coal generator send energy?");

        if(instance.hasChanged())
            instance.save();
    }

}
