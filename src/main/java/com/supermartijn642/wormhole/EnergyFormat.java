package com.supermartijn642.wormhole;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created 11/18/2020 by SuperMartijn642
 */
public class EnergyFormat {

    private static EnergyType type = EnergyType.RF;

    public static void cycleEnergyType(boolean forward){
        type = EnergyType.values()[(type.ordinal() + (forward ? 1 : EnergyType.values().length - 1)) % EnergyType.values().length];
    }

    public static String formatEnergy(int energy){
        return type.convertEnergy(energy) + " " + type.unit;
    }

    public static String formatEnergyPerTick(int energy){
        return type.convertEnergy(energy) + " " + type.unit + "/t";
    }

    public static String formatCapacity(int energy, int capacity){
        return type.convertEnergy(energy) + " / " + type.convertEnergy(capacity) + " " + type.unit;
    }

    private enum EnergyType {
        RF("RF")/*, MJ("MJ")*/, FE("FE");

        private final String unit;

        EnergyType(String unit){
            this.unit = unit;
        }

        public String getUnit(){
            return this.unit;
        }

        public String convertEnergy(int energy){
            return NumberFormat.getNumberInstance(Locale.getDefault()).format(energy);
        }
    }
}
