package de.doridian.yiffbukkit.commands;

import org.bukkit.command.CommandSender;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("reloadads")
@Help("Reload ads")
@Level(3)
public class ReloadAdsCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) {
		plugin.adHandler.ReloadAds();
	}
}
