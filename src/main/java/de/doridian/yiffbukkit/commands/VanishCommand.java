package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("vanish")
@Help("Makes you invisible")
@Usage("[on|off]")
@Level(3)
@Permission("yiffbukkit.vanish")
public class VanishCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		final String playerName = ply.getName();
		if (plugin.vanish.vanishedPlayers.contains(playerName)) {
			if (argStr.equals("on"))
				throw new YiffBukkitCommandException("Already invisible!");

			plugin.vanish.unVanish(ply);
			playerHelper.sendServerMessage(playerName + " reappeared.", playerHelper.getPlayerLevel(ply));
		}
		else {
			if (argStr.equals("off"))
				throw new YiffBukkitCommandException("Already visible!");

			plugin.vanish.vanish(ply);
			playerHelper.sendServerMessage(playerName + " vanished.", playerHelper.getPlayerLevel(ply));
		}

	}
}
