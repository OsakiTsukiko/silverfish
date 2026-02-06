package osaki.silverfish;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatListener implements Listener {
    private final JavaPlugin plugin;
    private final String webhookUrl;

    public ChatListener(Silverfish plugin) {
        this.plugin = plugin;
        this.webhookUrl = plugin.chatWebhookUrl;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String senderName = sender.getName();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String discordMessage = "**" + senderName + "**: " + message;
        if (webhookUrl != null) plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            Util.SendWebhook(discordMessage, webhookUrl, plugin.getLogger());
        });
    }
}
