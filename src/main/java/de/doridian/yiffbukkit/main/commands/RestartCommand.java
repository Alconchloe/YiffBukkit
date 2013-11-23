package de.doridian.yiffbukkit.main.commands;

import de.doridian.multicraft.api.MulticraftAPI;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.AbusePotential;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Names("restart")
@Permission("yiffbukkit.admin.restart")
@AbusePotential
public class RestartCommand extends ICommand {
	int taskID = -1;
	RestartRunnable restarter;

	@Override
	public void run(final CommandSender sender, String[] args, String argStr) throws YiffBukkitCommandException {
		if(taskID >= 0) {
			plugin.getServer().getScheduler().cancelTask(taskID);
			taskID = -1;
			plugin.playerHelper.sendServerMessage("Restart cancelled!");
			return;
		}

		final long time;
		if (args.length == 0) {
			time = 120;
		}
		else {
			try {
				time = Long.parseLong(args[0]);
			}
			catch (NumberFormatException e) {
				throw new YiffBukkitCommandException("Number expected.", e);
			}
		}

		restarter = new RestartRunnable(time);
		restarter.taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, restarter, 10, 10);
		taskID = restarter.taskID;
	}

	private class RestartRunnable implements Runnable {
		long lasttimeleft = 0;
		int taskID;
		long endtime;

		public RestartRunnable(long duration) {
			endtime = System.currentTimeMillis() + (duration * 1000);
			announceInChat(duration);
		}

		@Override
		public void run() {
			long timeleft = (endtime - System.currentTimeMillis()) / 1000;
			if(timeleft == lasttimeleft) return;
			lasttimeleft = timeleft;
			if(timeleft <= 0) {
				plugin.getServer().getScheduler().cancelTask(taskID);
				restartServer();
			} else if(timeleft > 60) {
				if((timeleft % 60) == 0) {
					announceInChat(timeleft);
				}
			} else if(timeleft > 10) {
				if((timeleft % 10) == 0) {
					announceInChat(timeleft);
				}
			} else {
				announceInChat(timeleft);
				if(timeleft == 2) {
					for(Player ply : plugin.getServer().getOnlinePlayers()) {
						ply.kickPlayer("Server is restarting! Reconnecting instantly will slow down the restart!");
					}
				} else if(timeleft == 1) {
					plugin.getServer().savePlayers();
				}
			}
		}
	}

	public void announceInChat(long timeleft) {
		plugin.playerHelper.sendServerMessage("Server restarting in " + timeleft + " seconds!");
	}

	private static final String ENDPOINT_URL = "http://panel.mc.doridian.de/api.php";
	private static final String API_USER = "admin";
	private static final String API_KEY = "06ffd261c790e2d31d66";

	public static void restartServer() {
		//plugin.getServer().shutdown();
		final MulticraftAPI api = new MulticraftAPI(ENDPOINT_URL, API_USER, API_KEY);

		api.call("restartServer", Collections.singletonMap("id", "1"));
	}
}
