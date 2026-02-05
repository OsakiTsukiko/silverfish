package osaki.silverfish;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JoinLeaveListener implements Listener {
    private final JavaPlugin plugin;
    private final String webhookUrl;
    private final String discordInvite;

    private final List<Pair<String, Instant>> joinTimestamp = new ArrayList<>();

    public JoinLeaveListener(Silverfish plugin) {
        this.plugin = plugin;
        this.webhookUrl = plugin.joinLeaveWebhookUrl;
        this.discordInvite = plugin.discordInvite;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String player_name = player.getName();

        Component joinMessage = Component.text(player_name, NamedTextColor.GREEN)
                .append(Component.text(" joined the game", NamedTextColor.YELLOW));
        event.joinMessage(joinMessage);
        if (discordInvite != null)
            plugin.getServer().getScheduler().runTask(plugin, () -> { // delay after join event
                Component clickableMessage = Component.text("Click here to join our Discord!")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(discordInvite))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Opens your browser!")
                        ));
                player.sendMessage(clickableMessage);
            });

        joinTimestamp.add(Pair.of(player_name, Instant.now()));

        if (webhookUrl == null) return;
        Util.SendWebhook("**" + player_name + "** joined the server!", webhookUrl, plugin.getLogger());
        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
        if (onlinePlayers.size() == 1) {
            Util.SendWebhook("@everyone **" + player_name + "** is all alone, care to join them?", webhookUrl, plugin.getLogger());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String player_name = player.getName();

        Component quitMessage = Component.text(player_name, NamedTextColor.RED)
                .append(Component.text(" left the game", NamedTextColor.YELLOW));
        event.quitMessage(quitMessage);

        Pair<String, Instant> found = null;

        for (Pair<String, Instant> p : joinTimestamp) {
            if (p.left().equals(player_name)) {
                found = p;
                break;
            }
        }

        if (found == null) {
            // alert
            return;
        }

        Instant joinTime = found.right();
        joinTimestamp.remove(found);

        Instant leaveTime = Instant.now();
        Duration elapsed = Duration.between(joinTime, leaveTime);

        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutes() % 60;
        long seconds = elapsed.getSeconds() % 60;

        if (webhookUrl == null) return;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) sb.append(minutes).append("m");
        if (hours == 0 && minutes == 0) sb.append(seconds).append("s");
        Util.SendWebhook("**" + player_name + "** left the server after **" + sb.toString().trim() + "**", webhookUrl, plugin.getLogger());
    }
}
