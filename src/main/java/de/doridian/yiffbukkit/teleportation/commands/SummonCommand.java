package de.doridian.yiffbukkit.teleportation.commands;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import de.doridian.yiffbukkit.main.util.PlayerFindException;
import org.bukkit.entity.Player;

@Names({"summon", "tphere"})
@Help("Teleports the specified user to you")
@Usage("<name>")
@Permission("yiffbukkit.teleport.summon")
public class SummonCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws PlayerFindException, PermissionDeniedException {
		Player otherply = playerHelper.matchPlayerSingle(args[0]);

		if (!playerHelper.canSummon(ply, otherply))
			throw new PermissionDeniedException();

		plugin.playerHelper.teleportWithHistory(otherply, ply);

		playerHelper.sendServerMessage(ply.getName() + " summoned " + otherply.getName());
	}
}
