package osaki.silverfish;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Silverfish extends JavaPlugin {
    public String chatWebhookUrl = null;
    public String joinLeaveWebhookUrl = null;
    public String discordInvite = null;

    public final Map<UUID, UUID> tpaRequests = new HashMap<>();

    private AfkManager afkManager;

    private Connection connection;
    private WaypointDB waypointDB;

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

        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();

            File dbFile = new File(getDataFolder(), "waypoints.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            waypointDB = new WaypointDB(connection, this);
            waypointDB.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        SuicideListener suicideListener = new SuicideListener();
        getServer().getPluginManager().registerEvents(suicideListener, this);
        getServer().getPluginManager().registerEvents(new SleepListener(this), this);
        afkManager = new AfkManager(this);
        getServer().getPluginManager().registerEvents(afkManager, this);

        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("suicide").setExecutor(new SuicideCommand(suicideListener));
        getCommand("afk").setExecutor(new AfkCommand(afkManager));

        WaypointCommands wp_cmds = new WaypointCommands(waypointDB, this);
        getCommand("wp").setExecutor(wp_cmds);
        getCommand("wpset").setExecutor(wp_cmds);
        getCommand("wpdel").setExecutor(wp_cmds);
        getCommand("wpls").setExecutor(wp_cmds);

        getLogger().info("Silverfish enabled!");
    }

    @Override
    public void onDisable() {
        if (afkManager != null) {
            afkManager.removeAllAfkOnDisable();
        }

        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
