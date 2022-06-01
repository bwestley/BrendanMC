package us.westley.brendan.BrendanMC;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

public class ToolListener implements Listener {
    private final BrendanMC plugin;

    public ToolListener(BrendanMC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        // From the tool's description:
        // Right-click on a ${tower.material} to create a defense tower.
        // Right-click on a defense tower to open its configuration menu.
        // Left-click on a defense tower to enable and disable it.


        if (!event.hasItem() || !ToolFactory.isTool(event.getItem())) return; // The tool did not click the defense tower.

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return; // No block was clicked.

        Location location = clickedBlock.getLocation();
        Player player = event.getPlayer();
        DefenseTower defenseTower = plugin.defenseTowers.getDefenseTower(location);

        if (event.getAction().isRightClick()) {
            if (defenseTower == null) {
                if (clickedBlock.getType().name().equals(plugin.config.getString("tower.material"))) {
                    // The source block was right-clicked: create a new defense tower.
                    plugin.defenseTowers.defenseTowers.add(new DefenseTower(
                            location,
                            10,
                            2,
                            EnumSet.of(DefenseTowerTarget.HOSTILE),
                            event.getPlayer().getUniqueId(),
                            new HashSet<>(),
                            false
                    ));
                    player.sendActionBar(Component.text("Created Turret", TextColor.color(0, 200, 0)));
                    event.setCancelled(true);
                }
            } else {
                // A defense tower was right-clicked: open the configuration menu.
                // TODO: Defense tower configuration menu.
                event.setCancelled(true);
            }
        }
        if (event.getAction().isLeftClick()) {
            if (defenseTower == null) return; // Not a defense tower.
            // A defense tower was left-clicked: toggle its enabled state.
            defenseTower.enabled ^= true;
            player.sendActionBar(defenseTower.enabled
                    ? Component.text("Enabled Turret", TextColor.color(0, 200, 0))
                    : Component.text("Disabled Turret", TextColor.color(200, 0, 0))
            );
            event.setCancelled(true);
        }
    }
}
