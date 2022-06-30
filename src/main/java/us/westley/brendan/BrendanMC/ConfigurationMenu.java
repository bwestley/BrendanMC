package us.westley.brendan.BrendanMC;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigurationMenu {
    public Inventory inventory;
    public DefenseTower defenseTower;
    public Player player;
    public ConfigurationState configurationState;
    public BrendanMC plugin;
    private final List<OfflinePlayer> playerSelection;

    public ConfigurationMenu(DefenseTower defenseTower, Player player, BrendanMC plugin) {
        this.defenseTower = defenseTower;
        this.player = player;
        configurationState = ConfigurationState.MAIN;
        this.plugin = plugin;
        playerSelection = new ArrayList<>();

        // Create configuration menu inventory.
        inventory = Bukkit.createInventory(
                null,
                6 * 9,
                Component.text(BrendanMC.getUsername(defenseTower.owner) + "'s Defense Tower", TextColor.color(0, 0, 0))
        );

        setInventoryItems();
    }

    private void setInventoryItems() {
        // Inventory layout:
        // 00 01 02 03 04 05 06 07 08
        // 09 10 11 12 13 14 15 16 17
        // 18 19 20 21 22 23 24 25 26
        // 27 28 29 30 31 32 33 34 35
        // 36 37 38 39 40 41 42 43 44
        // 45 46 47 48 49 50 51 52 53

        switch (configurationState) {
            case MAIN -> {
                inventory.clear();
                
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
                    name = BrendanMC.getUsername(allyUUID);

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
            case ADD_ALLY -> {
                inventory.clear();
                playerSelection.clear();

                // Players that are not allies.
                int i = 0;
                for (OfflinePlayer offlinePlayer: Bukkit.getWhitelistedPlayers()) {
                    if (i > 52) break; // The configuration inventory is filled.
                    if (defenseTower.alliedPlayers.contains(offlinePlayer.getUniqueId()) // Player is already an ally.
                            || defenseTower.owner.equals(offlinePlayer.getUniqueId())) continue; // Player is the defense tower's owner.

                    playerSelection.add(offlinePlayer);

                    String name = offlinePlayer.getName();
                    if (name == null) name = "[NO USERNAME]";

                    // Create player head.
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    meta.setOwningPlayer(offlinePlayer);
                    meta.displayName(createComponent(name));
                    meta.lore(List.of(
                            createComponent(offlinePlayer.isOnline() ? "Online" : "Offline"),
                            createComponent("UUID: " + offlinePlayer.getUniqueId())
                    ));
                    item.setItemMeta(meta);
                    inventory.setItem(i, item);
                    i++;
                }

                // Back
                inventory.setItem(53, createGUIItem(
                        Material.RED_CONCRETE,
                        Component.text("Back", TextColor.color(255, 0, 0)),
                        true
                ));
            }
            case REMOVE_ALLY -> {
                inventory.clear();
                playerSelection.clear();

                // Players that are not allies.
                int i = 0;
                for (UUID offlinePlayerUUID: defenseTower.alliedPlayers) {
                    if (i > 52) break; // The configuration inventory is filled.

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(offlinePlayerUUID);
                    playerSelection.add(offlinePlayer);

                    String name = offlinePlayer.getName();
                    if (name == null) name = "[NO USERNAME]";

                    // Create player head.
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) item.getItemMeta();
                    meta.setOwningPlayer(offlinePlayer);
                    meta.displayName(createComponent(name));
                    meta.lore(List.of(
                            createComponent(offlinePlayer.isOnline() ? "Online" : "Offline"),
                            createComponent("UUID: " + offlinePlayerUUID)
                    ));
                    item.setItemMeta(meta);
                    inventory.setItem(i, item);
                    i++;
                }

                // Back
                inventory.setItem(53, createGUIItem(
                        Material.RED_CONCRETE,
                        Component.text("Back", TextColor.color(255, 0, 0)),
                        true
                ));
            }
            default -> throw new IllegalStateException("Unexpected value: " + configurationState);
        }
    }

    @Contract("_ -> new")
    private @NotNull Component createComponent(String text) {
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

    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Prevent items in the configuration inventory from being removed.
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        boolean refresh = true;

        switch (configurationState) {
            case MAIN -> {
                switch (slot) {
                    // Targets
                    case 12 -> defenseTower.toggleTarget(DefenseTowerTarget.HOSTILE);
                    case 13 -> defenseTower.toggleTarget(DefenseTowerTarget.ANIMAL);
                    case 14 -> defenseTower.toggleTarget(DefenseTowerTarget.PLAYER);
                    case 15 -> defenseTower.toggleTarget(DefenseTowerTarget.PET);
                    case 16 -> defenseTower.toggleTarget(DefenseTowerTarget.OTHER);

                    // Range
                    case 21 -> defenseTower.range += 5;
                    case 22 -> defenseTower.range++;
                    case 23 -> defenseTower.range--;
                    case 24 -> defenseTower.range -= 5;

                    // Allies
                    case 30 -> configurationState = ConfigurationState.ADD_ALLY;
                    case 31 -> configurationState = ConfigurationState.REMOVE_ALLY;

                    // Destroy
                    case 53 -> {
                        inventory.close();
                        defenseTower.location.getLocation().getBlock().breakNaturally();
                        plugin.defenseTowers.defenseTowers.remove(defenseTower);
                        player.sendActionBar(Component.text("Destroyed defense tower.", TextColor.color(255, 0, 0)));
                        refresh = false;
                    }

                    default -> refresh = false;
                }
            }
            case ADD_ALLY -> {
                if (slot == 53) configurationState = ConfigurationState.MAIN;
                else if (slot >= playerSelection.size()) refresh = false;
                else {
                    OfflinePlayer offlinePlayer = playerSelection.get(slot);
                    UUID offlinePlayerUUID = offlinePlayer.getUniqueId();

                    defenseTower.alliedPlayers.add(offlinePlayerUUID);
                    player.sendActionBar(Component.text(
                            "Added player \"" + BrendanMC.getUsername(offlinePlayer) + "\" as an ally to this defense tower.",
                            TextColor.color(0, 200, 0)
                    ));
                }
            }
            case REMOVE_ALLY -> {
                if (slot == 53) configurationState = ConfigurationState.MAIN;
                else if (slot >= playerSelection.size()) refresh = false;
                else {
                    OfflinePlayer offlinePlayer = playerSelection.get(slot);
                    UUID offlinePlayerUUID = offlinePlayer.getUniqueId();

                    defenseTower.alliedPlayers.remove(offlinePlayerUUID);
                    player.sendActionBar(Component.text(
                            "Removed player \"" + BrendanMC.getUsername(offlinePlayer) + "\" as an ally from this defense tower.",
                            TextColor.color(0, 200, 0)
                    ));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + configurationState);
        }

        // Reset the configuration inventory's items to reflect the updated state.
        if (refresh) setInventoryItems();
    }
}
