package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names({"conv", "conversation"})
@Help("Opens or closes a conversation with the given player. This means that all your chat is going to them until you close the conversation by running the command without parameters.")
@Usage("[<name>]")
@Level(0)
public class ConversationCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		final String playerName = ply.getName();
		if (argStr.isEmpty()) {
			String otherName = playerHelper.conversations.get(playerName);
			if (otherName == null)
				throw new YiffBukkitCommandException("No conversation to close.");

			playerHelper.conversations.remove(playerName);

			playerHelper.SendDirectedMessage(ply, "Closed conversation with "+otherName+".");
			return;
		}

		final Player otherply = playerHelper.MatchPlayerSingle(argStr);
		final String otherName = otherply.getName();
		playerHelper.conversations.put(playerName, otherName);
		
		playerHelper.SendDirectedMessage(ply, "Opened conversation with "+otherName+".");
	}
}
