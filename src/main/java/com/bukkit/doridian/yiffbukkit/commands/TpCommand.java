package com.bukkit.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import com.bukkit.doridian.yiffbukkit.YiffBukkit;

public class TpCommand extends ICommand {
	public int GetMinLevel() {
		return 1;
	}
	
	public TpCommand(YiffBukkit plug) {
		plugin = plug;
	}

	public void Run(Player ply, String[] args, String argStr) {
		Player otherply = plugin.playerHelper.MatchPlayerSingle(ply, args[0]);
		if(otherply == null) return;
		if(plugin.playerHelper.GetPlayerLevel(ply) < plugin.playerHelper.GetPlayerLevel(otherply)) {
			plugin.playerHelper.SendPermissionDenied(ply);
			return;
		}
		
		ply.teleportTo(otherply);
		
		plugin.playerHelper.SendServerMessage(ply.getName() + " teleported to " + otherply.getName());
	}
	
	public String GetHelp() {
		return "Teleports you to the specified user";
	}

	public String GetUsage() {
		return "<name>";
	}
}
