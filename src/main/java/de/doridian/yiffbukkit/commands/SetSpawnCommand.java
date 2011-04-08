package de.doridian.yiffbukkit.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.YiffBukkitPlayerListener;

public class SetSpawnCommand extends ICommand {
	public SetSpawnCommand(YiffBukkitPlayerListener playerListener) {
		super(playerListener);
	}

	@Override
	public int GetMinLevel() {
		return 5;
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {

		Location loc = ply.getLocation();
		ply.getWorld().setSpawnLocation(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
		playerHelper.SendServerMessage(ply.getName() + " changed the world respawn point.");
	}

	@Override
	public String GetHelp() {
		return "Change the world spawn point.";
	}

	@Override
	public String GetUsage() {
		return "";
	}
}
