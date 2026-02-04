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

    public ChatListener(JavaPlugin plugin, String webhookUrl) {
        this.plugin = plugin;
        this.webhookUrl = webhookUrl;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String senderName = PlainTextComponentSerializer.plainText().serialize(sender.displayName());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        String discordMessage = "**" + senderName + "**: " + message;
        Util.SendWebhook(discordMessage, webhookUrl, plugin.getLogger());
    }
}
