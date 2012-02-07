package de.doridian.yiffbukkit.warp.commands;

import de.doridian.yiffbukkit.main.commands.ICommand;
import de.doridian.yiffbukkit.main.commands.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.ICommand.Permission;
import org.bukkit.entity.Player;

@Names("home")
@Help("Teleports you to your home position (see /sethome)")
@Permission("yiffbukkitsplit.teleport.basic.home")
public class HomeCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr) {
		if (plugin.jailEngine.isJailed(ply)) {
			playerHelper.sendDirectedMessage(ply, "You are jailed!");
			return;
		}

		plugin.playerHelper.teleportWithHistory(ply, playerHelper.getPlayerHomePosition(ply));
		playerHelper.sendServerMessage(ply.getName() + " went home!");
	}
}
