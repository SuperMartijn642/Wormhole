package com.supermartijn642.wormhole;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class WormholeConfig {

    //  -- power --
    // idle = idlePowerDrain + portal size * sizePowerDrain
    // teleport = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain + dimensionPowerDrain

    static{
        Pair<WormholeConfig,ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(WormholeConfig::new);
        CONFIG_SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final WormholeConfig INSTANCE;

    public ForgeConfigSpec.BooleanValue requireCorners;
    public ForgeConfigSpec.IntValue maxPortalSize;
    public ForgeConfigSpec.BooleanValue requireDimensionalCore; // TODO change 'dimensional core' to the actual name

    public ForgeConfigSpec.IntValue basicDeviceTargetCount;
    public ForgeConfigSpec.IntValue advancedDeviceTargetCount;

    public ForgeConfigSpec.BooleanValue requirePower;
    public ForgeConfigSpec.IntValue idlePowerDrain;
    public ForgeConfigSpec.DoubleValue sizePowerDrain;
    public ForgeConfigSpec.IntValue travelPowerDrain;
    public ForgeConfigSpec.DoubleValue distancePowerDrain;
    public ForgeConfigSpec.IntValue dimensionPowerDrain;

    public ForgeConfigSpec.IntValue stabilizerEnergyCapacity;
    public ForgeConfigSpec.IntValue basicEnergyCellCapacity;
    public ForgeConfigSpec.IntValue advancedEnergyCellCapacity;

    public ForgeConfigSpec.IntValue stabilizerTargetCapacity;
    public ForgeConfigSpec.IntValue basicTargetCellCapacity;
    public ForgeConfigSpec.IntValue advancedTargetCellCapacity;

    public ForgeConfigSpec.IntValue coalGeneratorPower;
    public ForgeConfigSpec.IntValue coalGeneratorCapacity;
    public ForgeConfigSpec.IntValue coalGeneratorRange;

    private WormholeConfig(ForgeConfigSpec.Builder builder){
        builder.push("General");
        this.requireCorners = builder.worldRestart().comment("If true, a portal will require frame blocks at its corners. Previously build portals won't be affected.").define("requireCorners", false);
        this.maxPortalSize = builder.worldRestart().comment("How big can the area inside a portal be? Higher numbers can impact performance when activating a portal. Previously build portals won't be affected.").defineInRange("maxPortalSize", 400, 1, 2000);
        this.requireDimensionalCore = builder.worldRestart().comment("Does a portal require a dimensional core for interdimensional travel? (WIP)").define("requireDimensionalCore", true); // TODO remove WIP
        builder.pop();
        builder.push("Target Devices");
        this.basicDeviceTargetCount = builder.worldRestart().comment("The maximum number of targets that can be stored in the basic target definition device.").defineInRange("basicDeviceTargetCount",1,1,10);
        this.advancedDeviceTargetCount = builder.worldRestart().comment("The maximum number of targets that can be stored in the advanced target definition device.").defineInRange("advancedDeviceTargetCount",5,1,10);
        builder.pop();
        builder.push("Power Consumption");
        builder.comment("Power consumption will be calculated as follows:",
            "Idle power drain = idlePowerDrain + portal size * sizePowerDrain",
            "Interdimensional teleport power drain = travelPowerDrain + dimensionPowerDrain",
            "Same dimension teleport power drain = travelPowerDrain + (target distance)^(1/2) * distancePowerDrain",
            "",
            "If false, a portal will not require power.");
        this.requirePower = builder.worldRestart().define("requirePower", true);
        this.idlePowerDrain = builder.worldRestart().comment("How much power will a portal drain per tick when idle?").defineInRange("idlePowerDrain", 10, 0, 1000000);
        this.sizePowerDrain = builder.worldRestart().comment("How much idle power does the portal drain per block of its size?").defineInRange("sizePowerDrain", 0.1, 0, 1000000d);
        this.travelPowerDrain = builder.worldRestart().comment("How much power will be drained when an entity is teleported?").defineInRange("travelPowerDrain", 100,0,1000000);
        this.distancePowerDrain = builder.worldRestart().comment("How much energy is drained when an entity is teleported multiplied by the distance?").defineInRange("distancePowerDrain", 0.5, 0, 1000000d);
        this.dimensionPowerDrain = builder.worldRestart().comment("How much energy is drained when an entity is teleported to another dimension?").defineInRange("dimensionPowerDrain", 400, 0, 1000000);
        builder.pop();
        builder.push("Energy Cells");
        this.stabilizerEnergyCapacity = builder.worldRestart().comment("The amount of energy the portal stabilizer can store.").defineInRange("stabilizerEnergyCapacity", 1000, 1, 1000000);
        this.basicEnergyCellCapacity = builder.worldRestart().comment("The amount of energy the basic energy cell can store.").defineInRange("basicEnergyCellCapacity", 2000, 1, 1000000);
        this.advancedEnergyCellCapacity = builder.worldRestart().comment("The amount of energy the advanced energy cell can store.").defineInRange("advancedEnergyCellCapacity", 10000, 1, 1000000);
        builder.pop();
        builder.push("Target Cells");
        this.stabilizerTargetCapacity = builder.worldRestart().comment("The number of targets the portal stabilizer can store.").defineInRange("stabilizerTargetCapacity", 1, 1, 20);
        this.basicTargetCellCapacity = builder.worldRestart().comment("The number of targets the basic target cell can store.").defineInRange("basicTargetCellCapacity", 4, 1, 20);
        this.advancedTargetCellCapacity = builder.worldRestart().comment("The number of targets the advanced target cell can store.").defineInRange("advancedTargetCellCapacity", 8, 1, 20);
        builder.pop();
        builder.push("Generators");
        this.coalGeneratorPower = builder.comment("How much energy does the coal generator generate per tick?").defineInRange("coalGeneratorPower", 10, 1, 1000000);
        this.coalGeneratorCapacity = builder.comment("How much energy can the coal generator store?").defineInRange("coalGeneratorCapacity", 5000, 1, 1000000);
        this.coalGeneratorRange = builder.comment("In what area (coalGeneratorRange^3) should the coal generator send energy?").defineInRange("coalGeneratorRange", 2, 1, 4);
        builder.pop();
    }

}
