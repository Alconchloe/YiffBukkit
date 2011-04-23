package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.PermissionDeniedException;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("leash")
@Help("Leashes or unleashes a player.")
@Usage("<name>")
@Level(4)
public class LeashCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		if (args.length < 1)
			throw new YiffBukkitCommandException("Not enough arguments");

		Player otherply = playerHelper.MatchPlayerSingle(args[0]);

		if (!playerHelper.CanSummon(ply, otherply))
			throw new PermissionDeniedException();

		if (playerHelper.toggleLeash(ply, otherply))
			playerHelper.SendServerMessage(ply.getName() + " leashed " + otherply.getName());
		else
			playerHelper.SendServerMessage(ply.getName() + " unleashed " + otherply.getName());
	}
}
