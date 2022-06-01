package us.westley.brendan.BrendanMC;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public interface ToolFactory {
    static ItemStack getTool(String blockTranslationKey) {
        // Create item.
        ItemStack item = new ItemStack(Material.STICK);

        // Add lore and set name.
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Configurator", TextColor.color(0, 255, 0)));
        meta.lore(List.of(
                Component.text("Right-click on a ", TextColor.color(255, 255, 255))
                        .append(Component.translatable(blockTranslationKey, TextColor.color(255, 255, 255)))
                        .append(Component.text(" to create a", TextColor.color(255, 255, 255))),
                Component.text("defense tower. Right-click on a defense tower", TextColor.color(255, 255, 255)),
                Component.text("to open its configuration menu. Left-click on", TextColor.color(255, 255, 255)),
                Component.text("a defense tower to enable and disable it.", TextColor.color(255, 255, 255))
        ));
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        // Add "BrendanMCTool"=true nbt tag to easily check later.
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean("BrendanMCTool", true);
        return nbtItem.getItem();
    }

    static boolean isTool(ItemStack item) {
        return (new NBTItem(item)).hasKey("BrendanMCTool"); // item has custom "BrendanMCTool" nbt tag.
    }
}
