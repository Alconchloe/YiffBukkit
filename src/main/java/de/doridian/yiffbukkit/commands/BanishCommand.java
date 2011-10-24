package de.doridian.yiffbukkit.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.PermissionDeniedException;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.util.PlayerFindException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("banish")
@Help("Banishes the specified user to the spawn and optionally resets their home location.")
@Usage("<name> [resethome]")
@Permission("yiffbukkit.teleport.banish")
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
