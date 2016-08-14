package net.ghostrealms.econo.command.type;

import net.ghostrealms.econo.Econo;
import net.ghostrealms.econo.utils.MessageUtils;
import net.ghostrealms.econo.command.SaneEconomyCommand;
import net.ghostrealms.econo.command.exception.CommandException;
import net.ghostrealms.econo.command.exception.type.usage.InvalidUsageException;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class SaneEcoCommand extends SaneEconomyCommand {
    @Override
    public String getPermission() {
        return "saneeconomy.admin";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "/<command> reload-database"
        };
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new InvalidUsageException();
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("reload-database")) {
            MessageUtils.sendMessage(sender, "Reloading database...");
            Econo.getInstance().getEconomyManager().getBackend().reloadDatabase();
            MessageUtils.sendMessage(sender, "Database reloaded.");
        } else {
            throw new InvalidUsageException();
        }
    }
}
