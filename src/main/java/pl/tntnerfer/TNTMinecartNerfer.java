package pl.tntnerfer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class TNTMinecartNerfer extends JavaPlugin {

    private static TNTMinecartNerfer instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getLogger().info("TNTMinecartNerfer enabled! Entity multiplier: "
                + getConfig().getDouble("entity-damage-multiplier", 1.0)
                + " | Block multiplier: "
                + getConfig().getDouble("block-damage-multiplier", 1.0));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tntnerfer.reload")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }
        reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "[TNTMinecartNerfer] Config reloaded! "
                + "Entity: " + getConfig().getDouble("entity-damage-multiplier", 1.0)
                + " | Blocks: " + getConfig().getDouble("block-damage-multiplier", 1.0));
        return true;
    }

    public static TNTMinecartNerfer getInstance() { return instance; }
}
