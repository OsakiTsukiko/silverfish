package osaki.silverfish;

import org.bukkit.plugin.java.JavaPlugin;

public final class Silverfish extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String webhookUrl = getConfig().getString("chat-webhook-url");
        if (!Util.ValidateWehbook(webhookUrl)) {
            getLogger().severe("Discord webhook URL is missing in config.yml! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this, webhookUrl), this);

        getLogger().info("Silverfish enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
