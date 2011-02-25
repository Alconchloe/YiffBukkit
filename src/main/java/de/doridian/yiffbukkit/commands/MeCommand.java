package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;

public class MeCommand extends ICommand {
	public int GetMinLevel() {
		return 0;
	}

	public MeCommand(YiffBukkit plug) {
		super(plug);
	}

	public void Run(Player ply, String[] args, String argStr) {
		plugin.getServer().broadcastMessage(playerHelper.GetPlayerTag(ply) + ply.getName() + " " + argStr);
	}

	public String GetHelp() {
		return "Well, its /me, durp";
	}

	public String GetUsage() {
		return "<stuff here>";
	}
}
