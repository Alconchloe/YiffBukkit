package de.doridian.yiffbukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("heal")
@Help("Heals a player fully or by the given amount.")
@Usage("[<name>] [<amount>]")
@Permission("yiffbukkit.players.heal")
public class HealCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		int amount;
		Player target;
		switch (args.length){
		case 0:
			//heal <name> - heal yourself fully
			amount = 20;
			target = asPlayer(commandSender);

			break;

		case 1:
			try {
				//heal <amount> - heal yourself by the given amount
				amount = Integer.parseInt(args[0]);
				target = asPlayer(commandSender);
			}
			catch (NumberFormatException e) {
				//heal <name> - heal someone fully
				amount = 20;
				target = playerHelper.matchPlayerSingle(args[0]);
			}
			break;

		default:
			try {
				//heal <amount> <name> - heal someone by the given amount
				amount = Integer.parseInt(args[0]);
				target = playerHelper.matchPlayerSingle(args[1]);
			}
			catch (NumberFormatException e) {
				//heal <name> <...> - not sure yet
				target = playerHelper.matchPlayerSingle(args[0]);

				try {
					//heal <name> <amount> - heal someone by the given amount
					amount = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException e2) {
					throw new YiffBukkitCommandException("Syntax error", e2);
				}
			}
			break;
		}

		target.setHealth(Math.min(20, target.getHealth() + amount));

		if (amount >= 20)
			playerHelper.sendServerMessage(commandSender.getName() + " fully healed " + target.getName() + ".");
		else
			playerHelper.sendServerMessage(commandSender.getName() + " healed " + target.getName() + " by "+amount+" points.");
	}
}
