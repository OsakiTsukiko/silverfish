package osaki.silverfish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AfkManager implements Listener {
    private final JavaPlugin plugin;
    private final Set<UUID> afkPlayers = new HashSet<>();

    public AfkManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setAfk(Player player) {
        UUID uuid = player.getUniqueId();
        if (!afkPlayers.contains(uuid)) {
            afkPlayers.add(uuid);
            player.setInvulnerable(true);
            player.setCollidable(false); // this doesn't work for players (untested for mobs)
            player.sendMessage(Component.text("You are now AFK. Move to return.").color(NamedTextColor.DARK_PURPLE));
            setAfkTab(player, true);
        } else {
            player.sendMessage(Component.text("You are already AFK.").color(NamedTextColor.RED));
        }
    }

    public void removeAfk(Player player) {
        UUID uuid = player.getUniqueId();
        if (afkPlayers.contains(uuid)) {
            afkPlayers.remove(uuid);
            player.setInvulnerable(false);
            player.setCollidable(true); // this doesn't work for players (untested for mobs)
            player.sendMessage(Component.text("You are no longer AFK.").color(NamedTextColor.DARK_PURPLE));
            setAfkTab(player, false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!afkPlayers.contains(player.getUniqueId())) return;

        if (
                event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()
        ) {
            removeAfk(player);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (afkPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (afkPlayers.contains(player.getUniqueId())) {
            removeAfk(player);
        }
    }

    public void removeAllAfkOnDisable() {
        for (UUID uuid : new HashSet<>(afkPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                removeAfk(player);
            }
        }
    }

    private void setAfkTab(Player player, boolean afk) {
        Team team = plugin.getServer().getScoreboardManager().getMainScoreboard().getTeam("AFK");
        if (team == null) {
            team = plugin.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("AFK");
            team.prefix(Component.text("AFK ", NamedTextColor.DARK_PURPLE));
        }

        if (afk) {
            team.addEntry(player.getName());
        } else {
            team.removeEntry(player.getName());
        }
    }
}
