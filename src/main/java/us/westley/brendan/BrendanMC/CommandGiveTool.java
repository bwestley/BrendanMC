package us.westley.brendan.BrendanMC;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandGiveTool implements CommandExecutor {
    private final BrendanMC plugin;

    public CommandGiveTool(BrendanMC plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player;
        if (args.length == 0) {
            if (sender instanceof Player) player = (Player) sender;
            else return false;
        } else {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) return false;
        }
        player.getInventory().addItem(plugin.tool);
        return true;
    }
}
