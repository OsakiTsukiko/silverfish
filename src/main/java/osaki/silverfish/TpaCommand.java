package osaki.silverfish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TpaCommand implements CommandExecutor {
    private final Silverfish plugin;

    public TpaCommand(Silverfish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /tpa <player>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("That player is not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("You can't teleport to yourself.");
            return true;
        }

        plugin.tpaRequests.put(target.getUniqueId(), player.getUniqueId());
        player.sendMessage("Teleport request sent to " + target.getName());

        Component msg = Component.text(player.getName() + " wants to teleport to you. ")
                .color(NamedTextColor.YELLOW);

        Component accept = Component.text("[ACCEPT]")
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/tpaccept " + player.getName()));

        Component deny = Component.text("[DENY]")
                .color(NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tpdeny " + player.getName()));

        target.sendMessage(msg.append(accept).append(Component.space()).append(deny));

        return true;
    }
}
