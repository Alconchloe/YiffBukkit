package de.doridian.yiffbukkit.permissions.commands;

import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.BooleanFlags;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissions;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import org.bukkit.entity.Player;

@Names({"checkoff","co"})
@Help("Check-Off list and system for YB")
@Usage("[[-f|-u|] name|-l|on|off]")
@BooleanFlags("ful")
@Permission("yiffbukkit.checkoff")
public class CheckOffCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		args = parseFlags(args);
		if (booleanFlags.contains('l')) {
			final StringBuilder reply = new StringBuilder("\u00a76CO: ");
			boolean first = true;
			for (String playerName : YiffBukkitPermissions.checkOffPlayers) {
				if (!first) {
					reply.append("\u00a7f, ");
				}
				if (isOnline(playerName)) {
					reply.append("\u00a72");
				} else {
					reply.append("\u00a74");
				}
				reply.append(playerName);
				first = false;
			}
			PlayerHelper.sendDirectedMessage(ply, reply.toString());
			return;
		}

		switch (args.length) {
		case 0:
			if (YiffBukkitPermissions.toggleDisplayCO(ply)) {
				PlayerHelper.sendDirectedMessage(ply, "Enabled CO display");
			} else {
				PlayerHelper.sendDirectedMessage(ply, "Disabled CO display");
			}
			return;

		case 1:
			switch (args[0]) {
			case "on":
				if (YiffBukkitPermissions.isDisplayingCO(ply)) {
					PlayerHelper.sendDirectedMessage(ply, "CO display already enabled");
				}
				else {
					YiffBukkitPermissions.toggleDisplayCO(ply);
					PlayerHelper.sendDirectedMessage(ply, "Enabled CO display");
				}
				return;

			case "off":
				if (YiffBukkitPermissions.isDisplayingCO(ply)) {
					YiffBukkitPermissions.toggleDisplayCO(ply);
					PlayerHelper.sendDirectedMessage(ply, "Disabled CO display");
				}
				else {
					PlayerHelper.sendDirectedMessage(ply, "CO display already disabled");
				}
				return;
			}
		}

		final String playerName = args[0];
		if (booleanFlags.contains('u')) {
			if (YiffBukkitPermissions.addCOPlayer(playerName)) {
				PlayerHelper.sendDirectedMessage(ply, "Added player "+playerName+" to CO");
			} else {
				PlayerHelper.sendDirectedMessage(ply, "Player "+playerName+" already on CO");
			}
			return;
		}

		if (!booleanFlags.contains('f') && isOnline(playerName))
			throw new YiffBukkitCommandException("Cannot check off online player without -f flag.");

		if (YiffBukkitPermissions.removeCOPlayer(playerName)) {
			PlayerHelper.sendDirectedMessage(ply, "Removed player "+playerName+" from CO");
		} else {
			PlayerHelper.sendDirectedMessage(ply, "Player "+playerName+" not found on CO");
		}
	}

	public boolean isOnline(String playerName) {
		Player plyply = plugin.getServer().getPlayerExact(playerName);
		//noinspection SimplifiableIfStatement
		if (plyply == null)
			return false;

		return plyply.isOnline();
	}
}
