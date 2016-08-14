package net.ghostrealms.econo.command.type;

import net.ghostrealms.econo.Econo;
import net.ghostrealms.econo.command.SaneEconomyCommand;
import net.ghostrealms.econo.command.exception.CommandException;
import net.ghostrealms.econo.command.exception.type.usage.NeedPlayerException;
import net.ghostrealms.econo.economy.economable.Economable;
import net.ghostrealms.econo.utils.MessageUtils;
import net.ghostrealms.econo.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class BalanceCommand extends SaneEconomyCommand {
    @Override
    public String getPermission() {
        return "saneeconomy.balance";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> [player]"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        String playerName;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            playerName = sender.getName();
        } else {
            playerName = args[0];

            if (!sender.hasPermission("saneeconomy.balance.other")) {
                MessageUtils.sendMessage(sender, "You don't have permission to check the balance of %s.", playerName);
                return;
            }
        }

        OfflinePlayer player = PlayerUtils.getOfflinePlayer(playerName);

        if (player == null) {
            MessageUtils.sendMessage(sender, "That player does not exist.");
            return;
        }

        MessageUtils.sendMessage(sender, "Balance for %s is %s.", playerName, Econo.getInstance().getEconomyManager().getFormattedBalance(Economable.wrap(player)));
    }
}
