package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;

public class JailCommand extends ICommand {
	public JailCommand(YiffBukkit plug) {
		super(plug);
	}

	@Override
	public int GetMinLevel() {
		return 4;
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) {
		if (args.length == 0) {
			playerHelper.SendDirectedMessage(ply, "Not enough arguments.");
			return;
		}

		Player otherply = playerHelper.MatchPlayerSingle(ply, args[0]);
		
		if (args.length == 1) {
			plugin.jailEngine.jailPlayer(otherply, true);
			playerHelper.SendServerMessage(ply.getName()+" sent "+otherply.getName()+" to jail.");
		}
		else if (args[1].equals("release") || args[1].equals("rel") || args[1].equals("r")) {
			plugin.jailEngine.jailPlayer(otherply, false);
			playerHelper.SendServerMessage(ply.getName()+" released "+otherply.getName()+" from jail.");
		}
		else {
			playerHelper.SendDirectedMessage(ply, "Invalid argument.");
		}
	}

	@Override
	public String GetHelp() {
		return "Sends someone to a previously defined jail cell.";
	}

	@Override
	public String GetUsage() {
		return "<name>[ release]";
	}


}
