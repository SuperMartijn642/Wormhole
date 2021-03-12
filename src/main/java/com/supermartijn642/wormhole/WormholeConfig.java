package com.supermartijn642.wormhole;

import com.supermartijn642.configlib.ModConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class WormholeConfig {

    //  -- power --
    // idle = idlePowerDrain + portal size * sizePowerDrain
    // teleport = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain + dimensionPowerDrain

    public static final Supplier<Boolean> requireCorners;
    public static final Supplier<Integer> maxPortalSize;
    public static final Supplier<Boolean> requireDimensionalCore; // TODO change 'dimensional core' to the actual name

    public static final Supplier<Integer> basicDeviceTargetCount;
    public static final Supplier<Integer> advancedDeviceTargetCount;

    public static final Supplier<Boolean> requirePower;
    public static final Supplier<Integer> idlePowerDrain;
    public static final Supplier<Double> sizePowerDrain;
    public static final Supplier<Integer> travelPowerDrain;
    public static final Supplier<Double> distancePowerDrain;
    public static final Supplier<Integer> dimensionPowerDrain;

    public static final Supplier<Integer> stabilizerEnergyCapacity;
    public static final Supplier<Integer> basicEnergyCellCapacity;
    public static final Supplier<Integer> advancedEnergyCellCapacity;

    public static final Supplier<Integer> stabilizerTargetCapacity;
    public static final Supplier<Integer> basicTargetCellCapacity;
    public static final Supplier<Integer> advancedTargetCellCapacity;

    public static final Supplier<Integer> coalGeneratorPower;
    public static final Supplier<Integer> coalGeneratorCapacity;
    public static final Supplier<Integer> coalGeneratorRange;

    static{
        ModConfigBuilder builder = new ModConfigBuilder("wormhole");

        builder.push("General");
        requireCorners = builder.comment("If true, a portal will require frame blocks at its corners. Previously build portals won't be affected.").define("requireCorners", false);
        maxPortalSize = builder.comment("How big can the area inside a portal be? Higher numbers can impact performance when activating a portal. Previously build portals won't be affected.").define("maxPortalSize", 400, 1, 2000);
        requireDimensionalCore = builder.comment("Does a portal require a dimensional core for interdimensional travel? (WIP)").define("requireDimensionalCore", true); // TODO remove WIP
        builder.pop();

        builder.push("Target Devices");
        basicDeviceTargetCount = builder.comment("The maximum number of targets that can be stored in the basic target definition device.").define("basicDeviceTargetCount", 1, 1, 10);
        advancedDeviceTargetCount = builder.comment("The maximum number of targets that can be stored in the advanced target definition device.").define("advancedDeviceTargetCount", 5, 1, 10);
        builder.pop();

        builder.push("Power Consumption");
        builder.categoryComment("Power consumption will be calculated as follows:\n" +
            "Idle power drain = idlePowerDrain + portal size * sizePowerDrain\n" +
            "Interdimensional teleport power drain = travelPowerDrain + dimensionPowerDrain\n" +
            "Same dimension teleport power drain = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain");
        requirePower = builder.comment("If false, a portal will not require power.").define("requirePower", true);
        idlePowerDrain = builder.comment("How much power will a portal drain per tick when idle?").define("idlePowerDrain", 1, 0, 1000000);
        sizePowerDrain = builder.comment("How much idle power does the portal drain per block of its size?").define("sizePowerDrain", 0.5, 0, 1000000d);
        travelPowerDrain = builder.comment("How much power will be drained when an entity is teleported?").define("travelPowerDrain", 100, 0, 1000000);
        distancePowerDrain = builder.comment("How much energy is drained when an entity is teleported multiplied by the distance?").define("distancePowerDrain", 0.5, 0, 1000000d);
        dimensionPowerDrain = builder.comment("How much energy is drained when an entity is teleported to another dimension?").define("dimensionPowerDrain", 400, 0, 1000000);
        builder.pop();

        builder.push("Energy Cells");
        stabilizerEnergyCapacity = builder.comment("The amount of energy the portal stabilizer can store.").define("stabilizerEnergyCapacity", 5000, 1, 1000000);
        basicEnergyCellCapacity = builder.comment("The amount of energy the basic energy cell can store.").define("basicEnergyCellCapacity", 10000, 1, 1000000);
        advancedEnergyCellCapacity = builder.comment("The amount of energy the advanced energy cell can store.").define("advancedEnergyCellCapacity", 25000, 1, 1000000);
        builder.pop();

        builder.push("Target Cells");
        stabilizerTargetCapacity = builder.comment("The number of targets the portal stabilizer can store.").define("stabilizerTargetCapacity", 1, 1, 20);
        basicTargetCellCapacity = builder.comment("The number of targets the basic target cell can store.").define("basicTargetCellCapacity", 4, 1, 20);
        advancedTargetCellCapacity = builder.comment("The number of targets the advanced target cell can store.").define("advancedTargetCellCapacity", 8, 1, 20);
        builder.pop();

        builder.push("Generators");
        coalGeneratorPower = builder.comment("How much energy does the coal generator generate per tick?").define("coalGeneratorPower", 10, 1, 1000000);
        coalGeneratorCapacity = builder.comment("How much energy can the coal generator store?").define("coalGeneratorCapacity", 5000, 1, 1000000);
        coalGeneratorRange = builder.comment("In what area (coalGeneratorRange^3) should the coal generator send energy?").define("coalGeneratorRange", 2, 1, 4);
        builder.pop();

        builder.build();
    }

}
