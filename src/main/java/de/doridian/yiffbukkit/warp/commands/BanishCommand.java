package de.doridian.yiffbukkit.warp.commands;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.ICommand;
import de.doridian.yiffbukkit.main.commands.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.ICommand.Usage;
import de.doridian.yiffbukkitsplit.util.PlayerFindException;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Names("banish")
@Help("Banishes the specified user to the spawn and optionally resets their home location.")
@Usage("<name> [resethome]")
@Permission("yiffbukkitsplit.teleport.banish")
public class BanishCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws PlayerFindException, PermissionDeniedException {
		boolean resetHome = args.length >= 2 && (args[1].equals("resethome") || args[1].equals("sethome") || args[1].equals("withhome"));

		Player otherply = playerHelper.matchPlayerSingle(args[0]);

		int level = playerHelper.getPlayerLevel(commandSender);
		int otherlevel = playerHelper.getPlayerLevel(otherply);

		// Players with the same levels can banish each other, but not reset each other's homes
		if (level < otherlevel || (level == otherlevel && resetHome))
			throw new PermissionDeniedException();

		Vector previousPos = otherply.getLocation().toVector();
		final Location teleportTarget = playerHelper.getPlayerSpawnPosition(otherply);
		otherply.teleport(teleportTarget);

		if (resetHome) {
			playerHelper.setPlayerHomePosition(otherply, teleportTarget);
		}
		else {
			Vector homePos = playerHelper.getPlayerHomePosition(otherply).toVector();

			final long unitsFromPrevious = Math.round(homePos.distance(previousPos));
			String unitsFromYou = "";
			try {
				unitsFromYou = Math.round(homePos.distance(asPlayer(commandSender).getLocation().toVector())) + "m from you and ";
			} catch (YiffBukkitCommandException e) { }
			final long unitsFromSpawn = Math.round(homePos.distance(teleportTarget.toVector()));

			playerHelper.sendDirectedMessage(
					commandSender, otherply.getName() + "'s home is " +
					unitsFromPrevious + "m from the previous location, " +
					unitsFromYou +
					unitsFromSpawn + "m from the spawn. Use '/banish " + otherply.getName() + " resethome' to move it to the spawn.");
		}

		playerHelper.sendServerMessage(commandSender.getName() + " banished " + otherply.getName() + (resetHome ? " and reset his/her home position!" : "!"));
	}
}
