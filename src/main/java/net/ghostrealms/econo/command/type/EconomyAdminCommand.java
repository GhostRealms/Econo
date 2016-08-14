package net.ghostrealms.econo.command.type;

import net.ghostrealms.econo.Econo;
import net.ghostrealms.econo.command.SaneEconomyCommand;
import net.ghostrealms.econo.command.exception.CommandException;
import net.ghostrealms.econo.command.exception.type.usage.InvalidUsageException;
import net.ghostrealms.econo.command.exception.type.usage.NeedPlayerException;
import net.ghostrealms.econo.command.exception.type.usage.TooFewArgumentsException;
import net.ghostrealms.econo.economy.economable.Economable;
import net.ghostrealms.econo.utils.I18n;
import net.ghostrealms.econo.utils.MessageUtils;
import net.ghostrealms.econo.utils.NumberUtils;
import net.ghostrealms.econo.utils.PlayerUtils;
import net.ghostrealms.econo.economy.EconomyManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyAdminCommand extends SaneEconomyCommand {
    @Override
    public String getPermission() {
        return "saneeconomy.ecoadmin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> <give/take/set> [player] <amount>"
        };
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new TooFewArgumentsException();
        }

        String subCommand = args[0];
        String sTargetPlayer;
        String sAmount;

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                throw new NeedPlayerException();
            }

            sTargetPlayer = sender.getName();
            sAmount = args[1];
        } else {
            sTargetPlayer = args[1];
            sAmount = args[2];
        }

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayer(sTargetPlayer);

        if (targetPlayer == null) {
            MessageUtils.sendMessage(sender, I18n._("That player does not exist."));
            return;
        }

        EconomyManager ecoMan = Econo.getInstance().getEconomyManager();
        Economable economable = Economable.wrap(targetPlayer);

        double amount = NumberUtils.parseAndFilter(ecoMan.getCurrency(), sAmount);

        if (amount <= 0) {
            MessageUtils.sendMessage(sender, I18n._("%s is not a positive number."), ((amount == -1) ? sAmount : String.valueOf(amount)));
            return;
        }

        if (subCommand.equalsIgnoreCase("give")) {
            double newAmount = ecoMan.addBalance(economable, amount);

            MessageUtils.sendMessage(sender, I18n._("Added %s to %s. Their balance is now %s."),
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("take")) {
            double newAmount = ecoMan.subtractBalance(economable, amount);

            MessageUtils.sendMessage(sender, I18n._("Took %s from %s. Their balance is now %s."),
                    ecoMan.getCurrency().formatAmount(amount),
                    sTargetPlayer,
                    ecoMan.getCurrency().formatAmount(newAmount)
            );
            return;
        }

        if (subCommand.equalsIgnoreCase("set")) {
            ecoMan.setBalance(economable, amount);
            MessageUtils.sendMessage(sender, I18n._("Balance for %s set to %s."), sTargetPlayer, ecoMan.getCurrency().formatAmount(amount));
            return;
        }

        throw new InvalidUsageException();
    }
}
