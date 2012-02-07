package de.doridian.yiffbukkit.main.commands;

import de.doridian.yiffbukkit.main.commands.ICommand;
import de.doridian.yiffbukkitsplit.StateContainer;
import de.doridian.yiffbukkitsplit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.ICommand.Usage;
import org.bukkit.command.CommandSender;

@Names("reloadconf")
@Help("Reloads a named config.")
@Usage("")
@Permission("yiffbukkitsplit.reload")
public class ReloadConfigCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		final boolean success;
		try {
			success = StateContainer.loadSingle(argStr);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new YiffBukkitCommandException("Exception caught while loading config. See Log.", e);
		}

		if (!success)
			throw new YiffBukkitCommandException("Config not found");

		playerHelper.sendDirectedMessage(commandSender, "Reloaded "+argStr+" config.");
	}
}
