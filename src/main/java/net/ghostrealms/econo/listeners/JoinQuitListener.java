package net.ghostrealms.econo.listeners;

import net.ghostrealms.econo.Econo;
import net.ghostrealms.econo.economy.economable.Economable;
import net.ghostrealms.econo.updates.GithubVersionChecker;
import net.ghostrealms.econo.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class JoinQuitListener implements Listener {
    private final Econo plugin;

    public JoinQuitListener(Econo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        Economable economable = Economable.wrap(player);
        double startBalance = plugin.getConfig().getDouble("economy.start-balance", 0.0D);

        /* A starting balance is configured AND they haven't been given it yet. */
        if ((startBalance > 0) && !plugin.getEconomyManager().accountExists(economable)) {
            plugin.getEconomyManager().setBalance(economable, startBalance);
            MessageUtils.sendMessage(player, "You've been issued a starting balance of %s!", plugin.getEconomyManager().getCurrency().formatAmount(startBalance));
        }

        /* Update notification */
        if (player.hasPermission("saneeconomy.update-notify") && GithubVersionChecker.isUpdateAvailable()) {
            MessageUtils.sendMessage(player, "An update is available! The currently-installed version is %s, but the newest available is %s. Please go to %s to update!", plugin.getDescription().getVersion(), GithubVersionChecker.getNewestVersion(), GithubVersionChecker.DOWNLOAD_URL);
        }
    }
}
