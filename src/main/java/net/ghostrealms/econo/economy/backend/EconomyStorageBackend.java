package net.ghostrealms.econo.economy.backend;

import net.ghostrealms.econo.economy.economable.Economable;

import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our economy storage backend - whatever we're using to store economy data.
 */
public interface EconomyStorageBackend {
    /**
     * Check whether a player has used the economy system before.
     * @param player Player
     * @return True if they have, false otherwise.
     */
    boolean accountExists(Economable player);

    /**
     * Get the balance of a player.
     * @param player Player
     * @return Player's current balance
     */
    double getBalance(Economable player);

    /**
     * Set the balance of a player, overwriting the old balance.
     * @param player Player
     * @param newBalance Player's new balance
     */
    void setBalance(Economable player, double newBalance);

    /**
     * Get the UUIDs of the players who have the most money, along with how much money they have.
     * @param amount Maximum number to get.
     * @return Map of player UUIDs to amounts.
     */
    Map<UUID, Double> getTopPlayerBalances(int amount);

    /**
     * Reload this backend's database from disk.
     */
    void reloadDatabase();

    /**
     * Reload this backend's top balances.
     */
    void reloadTopPlayerBalances();

    /**
     * Get the balances of all entities in this database.
     * @return Map of unique identifiers to balances.
     */
    Map<String, Double> getAllBalances();

    /**
     * Wait until all of the data in memory has been written out to disk.
     */
    void waitUntilFlushed();
}
