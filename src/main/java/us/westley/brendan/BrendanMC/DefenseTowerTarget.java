package us.westley.brendan.BrendanMC;

import java.util.EnumSet;

public enum DefenseTowerTarget {
    PETS, ANIMALS, HOSTILE, PLAYER, OTHER;
    public static final EnumSet<DefenseTowerTarget> ALL = EnumSet.allOf(DefenseTowerTarget.class);
}
