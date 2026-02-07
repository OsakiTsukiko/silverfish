package osaki.silverfish;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SuicideListener implements Listener {
    private final Set<UUID> suicides = new HashSet<>();

    public void markSuicide(Player p) {
        suicides.add(p.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();

        if (suicides.remove(p.getUniqueId())) {
            event.deathMessage(Component.text(p.getName() + " commited suicide.").color(NamedTextColor.RED));
        }
    }
}
