package us.westley.brendan.BrendanMC;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class BrendanMC extends JavaPlugin {
    FileConfiguration config = getConfig();
    Logger logger = getLogger();

    public DefenseTowers defenseTowers;
    ItemStack tool;

    @Override
    public void onEnable() {
        // Load configuration file.
        config.addDefault("tower.material", "DIAMOND_BLOCK");
        config.addDefault("tool.recipe.enabled", true);
        config.addDefault("tool.recipe.shape", List.of("  N", " S ", "O  "));
        config.addDefault("tool.recipe.items", Map.of(
                "N", "NETHER_STAR",
                "S", "STICK",
                "O", "OBSIDIAN"
        ));
        config.options().copyDefaults(true);
        saveConfig();

        // Load data from file.
        String dataFile = getDataFolder().getAbsolutePath() + "\\defenseTowers.dat";
        defenseTowers = DefenseTowers.loadData(dataFile);
        logger.info("Loaded " + defenseTowers.defenseTowers.size() + " defense towers from " + dataFile + ".");

        // Create tool.
        String materialName = config.getString("tower.material");
        String blockTranslationKey;
        if (materialName == null) blockTranslationKey = "block.minecraft.diamond_block";
        else {
            Material material = Material.getMaterial(materialName);
            if (material == null) blockTranslationKey = "block.minecraft.diamond_block";
            else blockTranslationKey = material.translationKey();
        }
        tool = ToolFactory.getTool(blockTranslationKey);

        // Register event listeners.
        getServer().getPluginManager().registerEvents(new ToolListener(this), this);

        // Create recipe.
        if (config.getBoolean("tool.recipe.enabled"))
            if (createToolRecipe()) logger.info("Successfully created tool recipe.");
            else logger.warning("Failed to create tool recipe.");

        // Register commands.
        Objects.requireNonNull(getCommand("givetool")).setExecutor(new CommandGiveTool(this));

        logger.info("BrendanMC enabled.");
    }

    @Override
    public void onDisable() {
        String dataFile = getDataFolder().getAbsolutePath() + "\\defenseTowers.dat";
        defenseTowers.saveData(new File(dataFile));
        logger.info("Saved " + defenseTowers.defenseTowers.size() + " defense towers to " + dataFile + ".");
        logger.info("BrendanMC disabled.");
    }

    private boolean createToolRecipe() {
        // Create recipe.
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey("brendanmc", "tool"), tool);

        // Set shape from configuration tool.recipe.shape.
        List<String> recipeShape = config.getStringList("tool.recipe.shape");
        switch (recipeShape.size()) {
            case 1 -> recipe.shape(recipeShape.get(0));
            case 2 -> recipe.shape(recipeShape.get(0), recipeShape.get(1));
            case 3 -> recipe.shape(recipeShape.get(0), recipeShape.get(1), recipeShape.get(2));
            default -> {
                logger.warning("Shape is not three lines.");
                throw new RuntimeException("Invalid recipe shape.");
            }
        }

        // Set ingredients from configuration tool.recipe.items.
        for (String line: recipeShape) {
            for (char key : line.toCharArray()) {
                if (key == ' ') continue;
                if (!config.isString("tool.recipe.items." + key)){
                    logger.warning("tool.recipe.items." + key + " is not a string.");
                    return false;
                }

                String materialName = config.getString("tool.recipe.items." + key);
                if (materialName == null) {
                    logger.warning("tool.recipe.items." + key + " is null.");
                    return false;
                }

                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    logger.warning("Material " + materialName + " does not exist.");
                    return false;
                }

                recipe.setIngredient(key, material);
            }
        }

        // Add recipe to server.
        boolean success = getServer().addRecipe(recipe);
        if (!success)
            logger.warning("Server did not add recipe.");

        return success;
    }
}
