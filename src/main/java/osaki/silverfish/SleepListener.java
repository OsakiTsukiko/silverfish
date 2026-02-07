package osaki.silverfish;

import io.papermc.paper.block.bed.BedEnterAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SleepListener implements Listener {
    private final JavaPlugin plugin;

    public SleepListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        BedEnterAction action = event.enterAction();
        if (!action.canSleep().success()) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        world.setTime(0);
        world.setStorm(false);
        world.setThundering(false);

        plugin.getServer().broadcast(Component.text(player.getName() + " slept. Good morning!").color(NamedTextColor.BLUE));
    }
}
