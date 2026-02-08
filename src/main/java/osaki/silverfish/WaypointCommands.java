package osaki.silverfish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.naming.Name;
import java.sql.SQLException;
import java.util.List;

public class WaypointCommands implements CommandExecutor {
    WaypointDB waypointDB;
    JavaPlugin plugin;

    public WaypointCommands(WaypointDB waypointDB, JavaPlugin plugin) {
        this.waypointDB = waypointDB;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "wpset":
                if (args.length < 1) {
                    player.sendMessage(Component.text("Usage: /wpset <waypoint-name>", NamedTextColor.YELLOW));
                    return true;
                }
                handleSet(player, args[0]);
                return true;

            case "wp":
                if (args.length < 1) {
                    player.sendMessage(Component.text("Usage: /wp <waypoint-name>", NamedTextColor.YELLOW));
                    return true;
                }
                handleTeleport(player, args[0]);
                return true;

            case "wpdel":
                if (args.length < 1) {
                    player.sendMessage(Component.text("Usage: /wpdel <waypoint-name>", NamedTextColor.YELLOW));
                    return true;
                }
                handleDelete(player, args[0]);
                return true;

            case "wpls":
                handleList(player);
                return true;
        }

        return false;
    }

    private void handleSet(Player player, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                waypointDB.setWaypoint(player, name);
                player.sendMessage(Component.text("Waypoint '" + name + "' saved.", NamedTextColor.GREEN));
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Failed to save waypoint.", NamedTextColor.RED));
            }
        });
    }

    private void handleTeleport(Player player, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Location loc = waypointDB.getWaypoint(player, name);

                if (loc == null) {
                    player.sendMessage(Component.text("Waypoint '" + name + "' not found.", NamedTextColor.RED));
                    return;
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> player.teleport(loc));
                player.sendMessage(Component.text("Teleported to '" + name + "'.", NamedTextColor.BLUE));
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Failed to load waypoint.", NamedTextColor.RED));
            }
        });
    }

    private void handleDelete(Player player, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean deleted = waypointDB.deleteWaypoint(player, name);

                if (deleted) {
                    player.sendMessage(Component.text("Deleted waypoint '" + name + "'.", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Waypoint '" + name + "' not found.", NamedTextColor.RED));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Failed to delete waypoint.", NamedTextColor.RED));
            }
        });
    }

    private void handleList(Player player) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<String> waypoints = waypointDB.listWaypoints(player);

                if (waypoints.isEmpty()) {
                    player.sendMessage(Component.text("You have no waypoints.", NamedTextColor.YELLOW));
                    return;
                }

                Component message = Component.text("Waypoints: ", NamedTextColor.YELLOW);
                for (String wp : waypoints) {
                    message = message.append(Component.text(wp + ", ", NamedTextColor.WHITE));
                }
                player.sendMessage(message);
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(Component.text("Failed to list waypoints.", NamedTextColor.RED));
            }
        });
    }
}
