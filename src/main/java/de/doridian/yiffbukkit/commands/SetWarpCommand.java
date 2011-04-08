package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitPlayerListener;
import de.doridian.yiffbukkit.warp.WarpDescriptor;
import de.doridian.yiffbukkit.warp.WarpException;

public class SetWarpCommand extends ICommand {
	
	@Override
	public int GetMinLevel() {
		return 3;
	}

	public SetWarpCommand(YiffBukkitPlayerListener playerListener) {
		super(playerListener);
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) throws WarpException {
		try {
			// TODO: error for argStr==""
			WarpDescriptor warp = plugin.warpEngine.setWarp(ply.getName(), argStr, ply.getLocation());
			playerHelper.SendDirectedMessage(ply, "Created warp �9" + warp.name + "�f here. Use '/warp help' to see how to modify it.");
		}
		catch (ArrayIndexOutOfBoundsException e) {
			playerHelper.SendDirectedMessage(ply, "Not enough arguments.");
		}
	}

	@Override
	public String GetHelp() {
		return "Creates a warp point with the specified name.";
	}

	@Override
	public String GetUsage() {
		return "<warp point name>";
	}
}
