package de.doridian.yiffbukkit.commands;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.util.Utils;

public class CompassCommand extends ICommand {
	public int GetMinLevel() {
		return 0;
	}

	public CompassCommand(YiffBukkit plug) {
		super(plug);
	}

	public void Run(Player ply, String[] args, String argStr) {
		float yaw = ply.getLocation().getYaw();
		playerHelper.SendDirectedMessage(ply, "Direction: "+Utils.yawToDirection(yaw)+" ("+Math.round(yaw)+")");
	}

	public String GetHelp() {
		return "Gives you your current bearing";
	}
}
