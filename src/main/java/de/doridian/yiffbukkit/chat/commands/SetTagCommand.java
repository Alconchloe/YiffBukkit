package de.doridian.yiffbukkit.chat.commands;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.*;
import de.doridian.yiffbukkit.main.util.Utils;
import org.bukkit.command.CommandSender;

@Names("settag")
@Help(
		"Sets the tag of the specified user.\n" +
		"Colors: \u00a70$0 \u00a71$1 \u00a72$2 \u00a73$3 \u00a74$4 \u00a75$5 \u00a76$6 \u00a77$7 \u00a78$8 \u00a79$9 \u00a7a$a \u00a7b$b \u00a7c$c \u00a7d$d \u00a7e$e \u00a7f$f\n" +
		"-r to specify a custom rank tag instead of a player tag."
)
@Usage("<name> <tag>|none")
@BooleanFlags("r")
@Permission("yiffbukkit.users.settag")
public class SetTagCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		args = parseFlags(args);
		final String otherName = playerHelper.completePlayerName(args[0], false);
		if (otherName == null) {
			return;
		}

		final String newTag = Utils.concatArray(args, 1, "").replace('$', '\u00a7');
		if (playerHelper.getPlayerLevel(commandSender) < playerHelper.getPlayerLevel(otherName))
			throw new PermissionDeniedException();

		final boolean useRankTag = booleanFlags.contains('r');
		final String tagTypeName = useRankTag ? "rank tag" : "tag";

		if (newTag.equals("none")) {
			playerHelper.setPlayerTag(otherName, null, useRankTag);
			playerHelper.sendServerMessage(commandSender.getName() + " reset "+tagTypeName+" of " + playerHelper.formatPlayerFull(otherName) + "\u00a7f!");
		}
		else {
			playerHelper.setPlayerTag(otherName, newTag, useRankTag);
			playerHelper.sendServerMessage(commandSender.getName() + " set "+tagTypeName+" of " + playerHelper.formatPlayerFull(otherName) + "\u00a7f!");
		}
	}
}
