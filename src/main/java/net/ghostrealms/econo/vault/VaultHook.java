package net.ghostrealms.econo.vault;

import net.ghostrealms.econo.Econo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class VaultHook {
    private final Econo plugin;
    private final Economy provider = new EconomyEcono();

    public VaultHook(Econo plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        Bukkit.getServicesManager().register(Economy.class, provider, plugin, ServicePriority.Normal);
    }

    public void unhook() {
        Bukkit.getServicesManager().unregister(Economy.class, provider);
    }
}
