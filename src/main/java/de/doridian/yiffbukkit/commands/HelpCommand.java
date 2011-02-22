package de.doridian.yiffbukkit.commands;

import java.util.Enumeration;
import java.util.Hashtable;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;

public class HelpCommand extends ICommand {
	public int GetMinLevel() {
		return 0;
	}

	public HelpCommand(YiffBukkit plug) {
		super(plug);
	}

	public void Run(Player ply, String[] args, String argStr) {
		int selflevel = plugin.playerHelper.GetPlayerLevel(ply);
		Hashtable<String,ICommand> commands = plugin.GetCommands();

		if(args.length > 0) {
			ICommand val = commands.get(args[0]);
			if(val == null || val.GetMinLevel() > selflevel) {
				plugin.playerHelper.SendDirectedMessage(ply, "Command not found!");
				return;
			}
			plugin.playerHelper.SendDirectedMessage(ply, val.GetHelp());
			plugin.playerHelper.SendDirectedMessage(ply, "Usage: /" + args[0] + " " + val.GetUsage());
		} else {
			String ret = "Available commands: /";
			Enumeration<String> e = commands.keys();
			while(e.hasMoreElements()) {
				String key = e.nextElement();
				ICommand val = commands.get(key);
				if(val.GetMinLevel() > selflevel) continue;
				ret += key + ", /";
			}
			ret = ret.substring(0,ret.length() - 3);
			plugin.playerHelper.SendDirectedMessage(ply, ret);
		}
	}

	public String GetHelp() {
		return "Prints command list if used without parameters or information about the specified command";
	}

	public String GetUsage() {
		return "[command]";
	}
}