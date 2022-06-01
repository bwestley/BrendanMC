package us.westley.brendan.BrendanMC;

import java.util.EnumSet;

public enum DefenseTowerTarget {
    PET, ANIMAL, HOSTILE, PLAYER, OTHER;
    public static final EnumSet<DefenseTowerTarget> ALL = EnumSet.allOf(DefenseTowerTarget.class);

    @Override
    public String toString() {
        return switch (this) {
            case PET -> "Pets";
            case ANIMAL -> "Animals";
            case HOSTILE -> "Monsters";
            case PLAYER -> "Players";
            case OTHER -> "Others";
        };
    }
}
