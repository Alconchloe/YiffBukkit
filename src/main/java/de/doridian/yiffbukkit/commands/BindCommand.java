package de.doridian.yiffbukkit.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import de.doridian.yiffbukkit.ToolBind;
import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.YiffBukkitCommandException;

public class BindCommand extends ICommand {
	private Set<String> filter = new HashSet<String>();

	{
		filter.add("/pm");
		filter.add("/say");
		filter.add("/me");
		filter.add("/throw");
		filter.add("/bind");
	}

	public BindCommand(YiffBukkit plug) {
		super(plug);
	}

	@Override
	public int GetMinLevel() {
		return 3;
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		Material toolType = ply.getItemInHand().getType();

		if (argStr.isEmpty()) {
			playerHelper.addToolMapping(ply, toolType, null);

			playerHelper.SendDirectedMessage(ply, "Unbound your current tool (�e"+toolType.name()+"�f).");

			return;
		}

		final Pattern pattern = Pattern.compile("^([^ ]+).*$");
		final List<String> commands = new ArrayList<String>();
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (String command : argStr.split(";")) {
			command = command.trim();
			if (command.charAt(0) != '/')
				command = '/' + command;

			Matcher matcher = pattern.matcher(command);

			if (!matcher.matches())
				continue;

			if (filter.contains(matcher.group(1)))
				throw new YiffBukkitCommandException("Command �9"+matcher.group(1)+"�f cannot be bound.");

			commands.add(command);

			if (!first)
				sb.append("�c; �9");
			first = false;

			sb.append(command);
		}
		final String commandString = sb.toString();

		ToolBind runnable = new ToolBind(commandString, ply) {
			public void run(PlayerInteractEvent event) {
				Player player = event.getPlayer();

				for (String command : commands) {
					player.chat(command);
				}
			}
		};

		playerHelper.addToolMapping(ply, toolType, runnable);

		playerHelper.SendDirectedMessage(ply, "Bound �9"+commandString+"�f to your current tool (�e"+toolType.name()+"�f). Right-click to use.");
	}

	@Override
	public String GetHelp() {
		return "Binds a command to your current tool. The leading slash is optional. Unbind by typing '/bind' without arguments.";
	}

	@Override
	public String GetUsage() {
		return "[<command>[;<command>[;<command> ...]]]";
	}

}
