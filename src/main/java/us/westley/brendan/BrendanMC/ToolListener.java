package us.westley.brendan.BrendanMC;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ToolListener implements Listener {
    private final BrendanMC plugin;
    @SuppressWarnings("FieldMayBeFinal")
    private HashMap<Inventory, DefenseTower> inventories;

    public ToolListener(BrendanMC plugin) {
        this.plugin = plugin;
        inventories = new HashMap<>();
    }

    public Inventory createInventory(DefenseTower defenseTower) {
        // Create configuration menu inventory.
        Inventory inventory = Bukkit.createInventory(
                null,
                6 * 9,
                Component.text(getPlayerName(defenseTower.owner) + "'s Defense Tower", TextColor.color(0, 0, 0))
        );

        setInventoryItems(defenseTower, inventory);

        inventories.put(inventory, defenseTower);
        return inventory;
    }

    private void setInventoryItems(DefenseTower defenseTower, Inventory inventory) {
        // Inventory layout:
        // 00 01 02 03 04 05 06 07 08
        // 09 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        // 36 37 38 39 40 41 42 43 44
        // 45 46 47 48 49 50 51 52 53

        // Targets
        inventory.setItem(10, createGUIItem(
                Material.OAK_SIGN,
                createComponent("Targets:"),
                false,
                defenseTower.targets.stream()
                        .map(target -> createComponent(target.toString()))
                        .toArray(Component[]::new)
        ));
        inventory.setItem(12, createGUIItem(Material.SKELETON_SKULL, Component.text("Monsters",
                defenseTower.targets.contains(DefenseTowerTarget.HOSTILE)
                        ? TextColor.color(0, 200, 0) : TextColor.color(200, 0, 0)
        ), false));
        inventory.setItem(13, createGUIItem(Material.WHEAT, Component.text("Animals",
                defenseTower.targets.contains(DefenseTowerTarget.ANIMAL)
                        ? TextColor.color(0, 200, 0) : TextColor.color(200, 0, 0)
        ), false));
        inventory.setItem(14, createGUIItem(Material.PLAYER_HEAD, Component.text("Players",
                defenseTower.targets.contains(DefenseTowerTarget.PLAYER)
                        ? TextColor.color(0, 200, 0) : TextColor.color(200, 0, 0)
        ), false));
        inventory.setItem(15, createGUIItem(Material.IRON_HORSE_ARMOR, Component.text("Pets",
                defenseTower.targets.contains(DefenseTowerTarget.PET)
                        ? TextColor.color(0, 200, 0) : TextColor.color(200, 0, 0)
        ), false));
        inventory.setItem(16, createGUIItem(Material.MINECART, Component.text("Others",
                defenseTower.targets.contains(DefenseTowerTarget.OTHER)
                        ? TextColor.color(0, 200, 0) : TextColor.color(200, 0, 0)
        ), false));

        // Range
        inventory.setItem(19, createGUIItem(
                Material.SPECTRAL_ARROW,
                createComponent("Range: " + defenseTower.range + "m"),
                false
        ));
        inventory.setItem(21, createGUIItem(Material.GREEN_CONCRETE, createComponent("+5"), false));
        inventory.setItem(22, createGUIItem(Material.GREEN_DYE, createComponent("+1"), false));
        inventory.setItem(23, createGUIItem(Material.RED_DYE, createComponent("-1"), false));
        inventory.setItem(24, createGUIItem(Material.RED_CONCRETE, createComponent("-5"), false));

        // Allies
        ArrayList<Component> allies = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        String name;
        for (UUID allyUUID: defenseTower.alliedPlayers) {
            name = getPlayerName(allyUUID);

            if (line.length() + name.length() > 35) {
                line.append(",");
                allies.add(Component.text(line.toString(), TextColor.color(255, 255, 255)));
                line.setLength(0);
                line.append(name);
            } else {
                if (line.length() != 0) line.append(", ");
                line.append(name);
            }
        }
        allies.add(Component.text(line.toString(), TextColor.color(255, 255, 255)));

        inventory.setItem(28, createGUIItem(
                Material.TOTEM_OF_UNDYING,
                createComponent(defenseTower.alliedPlayers.size() + " Allies:"),
                true,
                allies.toArray(Component[]::new)
        ));
        inventory.setItem(30, createGUIItem(Material.POPPY, createComponent("Add"), false));
        inventory.setItem(31, createGUIItem(Material.WITHER_ROSE, createComponent("Remove"), false));

        // Destroy
        inventory.setItem(53, createGUIItem(
                Material.RED_CONCRETE,
                Component.text("Destroy", TextColor.color(255, 0, 0)),
                true
        ));
    }

    private String getPlayerName(UUID uuid) {
        String name;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) name = "{" + uuid + "}";
        else name = player.getPlayerProfile().getName();
        if (name == null) name = "{" + uuid + "}";
        return name;
    }

    private Component createComponent(String text) {
        return Component.text(text, TextColor.color(255, 255, 255));
    }

    private @NotNull ItemStack createGUIItem(Material material, Component name, boolean shiny, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(lore));
        if (shiny) meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
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
                player.openInventory(createInventory(defenseTower));
                event.setCancelled(true);
            }
        }
        if (event.getAction().isLeftClick()) {
            if (defenseTower == null) return; // Not a defense tower.
            // A defense tower was left-clicked: toggle its enabled state.
            defenseTower.enabled ^= true;
            player.sendActionBar(defenseTower.enabled
                    ? Component.text("Enabled Defense Tower", TextColor.color(0, 200, 0))
                    : Component.text("Disabled Defense Tower", TextColor.color(200, 0, 0))
            );
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Process clicks in a configuration inventory.
        Inventory inventory = event.getInventory();
        DefenseTower defenseTower = inventories.get(inventory);
        if (defenseTower == null) return;

        // Prevent items in the configuration inventory from being removed.
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        boolean refresh = true;
        switch (slot) {
            // Targets
            case 12 -> toggleTarget(defenseTower.targets, DefenseTowerTarget.HOSTILE);
            case 13 -> toggleTarget(defenseTower.targets, DefenseTowerTarget.ANIMAL);
            case 14 -> toggleTarget(defenseTower.targets, DefenseTowerTarget.PLAYER);
            case 15 -> toggleTarget(defenseTower.targets, DefenseTowerTarget.PET);
            case 16 -> toggleTarget(defenseTower.targets, DefenseTowerTarget.OTHER);

            // Range
            case 21 -> defenseTower.range += 5;
            case 22 -> defenseTower.range++;
            case 23 -> defenseTower.range--;
            case 24 -> defenseTower.range -= 5;

            // Allies
            // TODO: Add and remove allied players.
            case 30 -> player.sendActionBar(createComponent("Adding an allied player is not yet implemented."));
            case 31 -> player.sendActionBar(createComponent("Removing an allied player is not yet implemented."));

            // Destroy
            case 53 -> {
                inventories.remove(inventory);
                inventory.close();
                plugin.defenseTowers.defenseTowers.remove(defenseTower);
                player.sendActionBar(Component.text("Destroyed defense tower.", TextColor.color(255, 0, 0)));
                refresh = false;
            }

            default -> refresh = false;
        }
        if (refresh) setInventoryItems(defenseTower, inventory);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Cancel dragging in a configuration inventory.
        if (isConfigurationInventory(event.getInventory())) event.setCancelled(true);
    }

    private void toggleTarget(EnumSet<DefenseTowerTarget> targets, DefenseTowerTarget target) {
        if (targets.contains(target)) targets.remove(target);
        else targets.add(target);
    }

    public boolean isConfigurationInventory(Inventory inventory) {
        for (Inventory inv: inventories.keySet())
            if (inventory.equals(inv)) return true;
        return false;
    }
}
