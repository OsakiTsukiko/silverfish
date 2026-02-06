package osaki.silverfish;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {
    private final Silverfish plugin;

    public TpDenyCommand(Silverfish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player target)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        if (args.length != 1) {
            target.sendMessage("Usage: /tpdeny <player>");
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


        requester.sendMessage("Your teleport request was denied.");
        target.sendMessage("Teleport request denied.");

        plugin.tpaRequests.remove(target.getUniqueId());

        return true;
    }
}
