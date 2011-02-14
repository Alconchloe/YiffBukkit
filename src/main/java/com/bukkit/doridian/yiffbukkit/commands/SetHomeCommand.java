package com.bukkit.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import com.bukkit.doridian.yiffbukkit.YiffBukkit;

public class SetHomeCommand extends ICommand {
	public int GetMinLevel() {
		return 0;
	}
	
	public SetHomeCommand(YiffBukkit plug) {
		plugin = plug;
	}

	public void Run(Player ply, String[] args, String argStr) {
		plugin.playerHelper.SetPlayerHomePosition(ply, ply.getLocation());
		plugin.playerHelper.SendDirectedMessage(ply, "Home location saved!");
	}
	
	public String GetHelp() {
		return "Sets your home position (see /home)";
	}

	public String GetUsage() {
		return "";
	}
}