package us.westley.brendan.BrendanMC;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;

public class ToolListener implements Listener {
    private final BrendanMC plugin;
    @SuppressWarnings("FieldMayBeFinal")
    private HashMap<Player, ConfigurationMenu> configurationMenus;

    public ToolListener(BrendanMC plugin) {
        this.plugin = plugin;
        configurationMenus = new HashMap<>();
    }

    @EventHandler
    public void onClick(@NotNull PlayerInteractEvent event) {
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
                    player.sendActionBar(Component.text("Created Defense Tower", TextColor.color(0, 200, 0)));
                    event.setCancelled(true);
                }
            } else {
                // A defense tower was right-clicked: open the configuration menu.
                if (checkPermission(player, defenseTower)) {
                    ConfigurationMenu configurationMenu = new ConfigurationMenu(defenseTower, player, plugin);
                    configurationMenus.put(player, configurationMenu);
                    player.openInventory(configurationMenu.inventory);
                }
                event.setCancelled(true);
            }
        }
        if (event.getAction().isLeftClick()) {
            if (defenseTower == null) return; // Not a defense tower.
            // A defense tower was left-clicked: toggle its enabled state.
            if (checkPermission(player, defenseTower)) {
                defenseTower.enabled ^= true;
                player.sendActionBar(defenseTower.enabled
                        ? Component.text("Enabled Defense Tower", TextColor.color(0, 200, 0))
                        : Component.text("Disabled Defense Tower", TextColor.color(200, 0, 0))
                );
            }
            event.setCancelled(true);
        }
    }

    private boolean checkPermission(@NotNull Player player, @NotNull DefenseTower defenseTower) {
        plugin.logger.info("%s %s".formatted(String.valueOf(defenseTower.owner), String.valueOf(player.getUniqueId())));
        if (defenseTower.owner.equals(player.getUniqueId())) return true;
        // The player is not the owner of the defense tower.
        player.sendActionBar(Component.text(
                String.format(
                    "You (%s) are not the owner (%s) of this defense tower.",
                    player.getName(),
                    BrendanMC.getUsername(defenseTower.owner)
                ), TextColor.color(200, 0, 0)
        ));
        return false;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (isConfigurationInventory(player, event.getInventory())) {
            configurationMenus.get(player).onInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Cancel dragging in a configuration inventory.
        if (isConfigurationInventory((Player) event.getWhoClicked(), event.getInventory()))
            event.setCancelled(true);
    }

    public boolean isConfigurationInventory(Player player, Inventory inventory) {
        ConfigurationMenu configurationMenu = configurationMenus.get(player);
        if (configurationMenu == null) return false;
        return inventory.equals(configurationMenu.inventory);
    }
}
