package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.PermissionDeniedException;
import de.doridian.yiffbukkit.util.PlayerFindException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("tp")
@Help("Teleports you to the specified user")
@Usage("<name>")
@Level(1)
public class TpCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws PlayerFindException, PermissionDeniedException {
		Player otherply = playerHelper.MatchPlayerSingle(args[0]);

		String playerName = ply.getName();
		String otherName = otherply.getName();

		if (!playerHelper.CanTp(ply, otherply))
			throw new PermissionDeniedException();

		ply.teleport(otherply);

		if (playerHelper.vanishedPlayers.contains(playerName)) {
			playerHelper.SendServerMessage(playerName + " teleported to " + otherName, 3);
		}
		else {
			playerHelper.SendServerMessage(playerName + " teleported to " + otherName);
		}
	}
}
