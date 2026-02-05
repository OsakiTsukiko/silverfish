package osaki.silverfish;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class Silverfish extends JavaPlugin {
    public String chatWebhookUrl = null;
    public String joinLeaveWebhookUrl = null;
    public String discordInvite = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getConfig().contains("discord", true)) {
            ConfigurationSection discordSection = getConfig().getConfigurationSection("discord");
            assert discordSection != null; // should be unreachable
            String invite = discordSection.getString("invite");
            if (invite != null && (invite.isBlank() || invite.equals("PUT_INVITE_URL_HERE")))
                getLogger().severe("discord.invite is missing from config.yaml");
            else discordInvite = invite;

            if (discordSection.contains("chat", true)) {
                ConfigurationSection chat = discordSection.getConfigurationSection("chat");
                assert chat != null; // should be unreachable
                String webhook_url = chat.getString("webhook-url");
                if (!Util.ValidateWehbook(webhook_url)) {
                    getLogger().severe("discord.chat.webhook-url is missing from config.yaml");
                }
                chatWebhookUrl = webhook_url;
            }

            if (discordSection.contains("join-leave", true)) {
                ConfigurationSection join_leave = discordSection.getConfigurationSection("join-leave");
                assert join_leave != null; // should be unreachable
                String webhook_url = join_leave.getString("webhook-url");
                if (!Util.ValidateWehbook(webhook_url)) {
                    getLogger().severe("discord.join-leave.webhook-url is missing from config.yaml");
                }
                joinLeaveWebhookUrl = webhook_url;
            }
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);

        getLogger().info("Silverfish enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
