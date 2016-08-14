package net.ghostrealms.econo;

import com.google.common.base.Strings;
import net.ghostrealms.econo.command.type.*;
import net.ghostrealms.econo.updates.GithubVersionChecker;
import net.ghostrealms.econo.command.SaneEconomyCommand;
import net.ghostrealms.econo.economy.Currency;
import net.ghostrealms.econo.economy.EconomyManager;
import net.ghostrealms.econo.economy.backend.EconomyStorageBackend;
import net.ghostrealms.econo.economy.backend.type.EconomyStorageBackendFlatfile;
import net.ghostrealms.econo.economy.backend.type.EconomyStorageBackendMySQL;
import net.ghostrealms.econo.economy.economable.EconomableGeneric;
import net.ghostrealms.econo.listeners.JoinQuitListener;
import net.ghostrealms.econo.utils.I18n;
import net.ghostrealms.econo.vault.VaultHook;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class Econo extends JavaPlugin {
    private static Econo instance;
    private EconomyManager economyManager;
    private VaultHook vaultHook;

    private static final Map<String, SaneEconomyCommand> COMMANDS = new HashMap<String, SaneEconomyCommand>() {{
        put("balance", new BalanceCommand());
        put("ecoadmin", new EconomyAdminCommand());
        put("pay", new PayCommand());
        put("saneeconomy", new SaneEcoCommand());
        put("balancetop", new BalanceTopCommand());
    }};

    public Econo() {
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        if (!loadEconomyBackend()) { /* Invalid backend type or connection error of some sort */
            shutdown();
            return;
        }

        loadCommands();
        loadListeners();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            vaultHook = new VaultHook(this);
            vaultHook.hook();
            getLogger().info("Hooked into Vault.");
        } else {
            getLogger().info("Not hooking into Vault because it isn't loaded.");
        }

        getServer().getScheduler().scheduleAsyncDelayedTask(this, GithubVersionChecker::checkUpdateAvailable);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            economyManager.getBackend().reloadTopPlayerBalances();
        }, 0, (20 * 300) /* Update baltop every 5 minutes */);
        I18n.getInstance().loadTranslations();
    }

    @Override
    public void onDisable() {
        if (vaultHook != null) {
            vaultHook.unhook();
        }

        economyManager.getBackend().waitUntilFlushed();
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (configFile.exists() && getConfig().getBoolean("debug", false)) {
            getLogger().info("Resetting configuration to default since debug == true.");
            configFile.delete();
            saveDefaultConfig();
            reloadConfig();
            getConfig().set("debug", true);
            saveConfig();
        } else {
            saveDefaultConfig();
            reloadConfig();
        }
    }

    private boolean loadEconomyBackend() {
        getLogger().info("Initializing currency...");
        Currency currency = Currency.fromConfig(getConfig(), "currency");
        getLogger().info("Initialized currency: " + currency.getPluralName());

        getLogger().info("Initializing economy storage backend...");
        String backendType = getConfig().getString("backend.type");
        String oldBackendType = getConfig().getString("old-backend.type", null);

        EconomyStorageBackend backend = loadBackend(backendType, "backend");

        if (backend == null) {
            getLogger().severe("Failed to load backend!");
            return false;
        }

        getLogger().info("Performing initial data load...");
        backend.reloadDatabase();
        getLogger().info("Data loaded!");

        if (!Strings.isNullOrEmpty(oldBackendType)) {
            getLogger().info("Old backend detected, converting... (This may take a minute or two.)");
            EconomyStorageBackend oldBackend = loadBackend(oldBackendType, "old-backend");
            if (oldBackend == null) {
                getLogger().severe("Failed to load old backend!");
                return false;
            }

            oldBackend.reloadDatabase();
            convertBackends(oldBackend, backend);
            getLogger().info("Data converted, removing old config section.");
            getConfig().set("old-backend", null);
            saveConfig();
        }

        economyManager = new EconomyManager(currency, backend);

        return true;
    }

    private EconomyStorageBackend loadBackend(String backendType, String configPrefix) {
        EconomyStorageBackend backend;

        if (backendType.equalsIgnoreCase("flatfile")) {
            String backendFileName = getConfig().getString(configPrefix + ".file", "economy.db");
            File backendFile = new File(getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendFlatfile(backendFile);
            getLogger().info("Initialized flatfile backend with file " + backendFile.getAbsolutePath());
        } else if (backendType.equalsIgnoreCase("mysql")) {
            String backendHost = getConfig().getString(configPrefix + ".host");
            int backendPort = getConfig().getInt(configPrefix + ".port", 3306);
            String backendDb = getConfig().getString(configPrefix + ".database");
            String backendUser = getConfig().getString(configPrefix + ".username");
            String backendPass = getConfig().getString(configPrefix + ".password");

            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", backendHost, backendPort, backendDb);

            EconomyStorageBackendMySQL mySQLBackend = new EconomyStorageBackendMySQL(jdbcUrl, backendUser, backendPass);
            backend = mySQLBackend;

            getLogger().info("Initialized MySQL backend to host " + backendHost);
            getLogger().info("Testing connection...");
            if (!mySQLBackend.testConnection()) {
                getLogger().severe("MySQL connection failed - cannot continue!");
                return null;
            }

            getLogger().info("Connection successful!");
        } else {
            getLogger().severe("Unknown storage backend " + backendType + "!");
            return null;
        }

        return backend;
    }

    private void convertBackends(EconomyStorageBackend old, EconomyStorageBackend newer) {
        old.getAllBalances().forEach((uniqueId, balance) -> {
            newer.setBalance(new EconomableGeneric(uniqueId), balance);
        });
        newer.waitUntilFlushed();
    }

    private void loadCommands() {
        getLogger().info("Initializing commands...");
        COMMANDS.forEach((name, command) -> getCommand(name).setExecutor(command));
        getLogger().info("Initialized commands.");
    }

    private void loadListeners() {
        getLogger().info("Initializing listeners...");
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getLogger().info("Initialized listeners.");
    }

    private void shutdown(){
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Get the current plugin instance.
     * @return Instance
     */
    public static Econo getInstance() {
        return instance;
    }

    /**
     * Get the logger for the plugin.
     * @return Plugin logger.
     */
    public static Logger logger(){
        return instance.getLogger();
    }
}
