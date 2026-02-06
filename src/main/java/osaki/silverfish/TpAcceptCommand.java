package osaki.silverfish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {
    private final Silverfish plugin;

    public TpAcceptCommand(Silverfish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player target)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        if (args.length != 1) {
            target.sendMessage("Usage: /tpaccept <player>");
            return true;
        }

        Player requester = Bukkit.getPlayer(args[0]);
        if (requester == null) {
            target.sendMessage("Player not online.");
            return true;
        }

        UUID storedRequesterId = plugin.tpaRequests.get(target.getUniqueId());
        if (storedRequesterId == null || !storedRequesterId.equals(requester.getUniqueId())) {
            target.sendMessage("No pending request from that player.");
            return true;
        }

        requester.teleport(target);

        requester.sendMessage("Teleport request accepted.");
        target.sendMessage("You accepted the teleport request.");

        plugin.tpaRequests.remove(target.getUniqueId());
        return true;
    }
}
