package us.westley.brendan.BrendanMC;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

public class DefenseTower implements Serializable {
    @Serial
    private static final long serialVersionUID = 4550156864234411752L;

    public final SerializableLocation location;
    public double range;
    public final double damage;
    public final EnumSet<DefenseTowerTarget> targets;
    public final UUID owner;
    public final HashSet<UUID> alliedPlayers;
    public boolean enabled;

    public DefenseTower(Location location, double range, double damage, EnumSet<DefenseTowerTarget> targets, UUID owner, HashSet<UUID> alliedPlayers, boolean enabled) {
        this.location = new SerializableLocation(location);
        this.range = range;
        this.damage = damage;
        this.targets = targets;
        this.owner = owner;
        this.alliedPlayers = alliedPlayers;
        this.enabled = enabled;
    }

    public boolean isEnemy(Entity entity) {
        return entity instanceof Damageable && ( // entity can be damaged.
                targets.contains(DefenseTowerTarget.PET) // Turret targets pets.
                        && entity instanceof Tameable // entity is tamable.
                        && ((Tameable) entity).getOwnerUniqueId() != owner // entity was not tamed by the turret's owner.
                        && !alliedPlayers.contains(((Tameable) entity).getOwnerUniqueId()) // entity was not tamed by an allied player.
                || targets.contains(DefenseTowerTarget.ANIMAL) // Turret targets animals.
                        && (entity instanceof Animals || entity instanceof Ambient) // entity is an animal (include bats).
                || targets.contains(DefenseTowerTarget.HOSTILE) // Turret targets hostile mobs.
                        && entity instanceof Monster // entity is a monster.
                || targets.contains(DefenseTowerTarget.PLAYER) // Turret targets players.
                        && ((Player) entity).getPlayerProfile().getId() != owner // entity is not the turret's owner.
                        && !alliedPlayers.contains(((Player) entity).getPlayerProfile().getId()) // entity is not an allied player.
                || targets.contains(DefenseTowerTarget.OTHER) // Turret targets other entities.
        );
    }

    public boolean canTarget(@NotNull Entity entity) {
        try {
            return entity.getLocation().distance(location.getLocation()) <= range && isEnemy(entity); // entity is within range and an enemy.
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void toggleTarget(DefenseTowerTarget target) {
        if (targets.contains(target)) targets.remove(target);
        else targets.add(target);
    }
}
