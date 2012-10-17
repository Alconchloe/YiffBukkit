package de.doridian.yiffbukkit.permissions.commands;

import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import de.doridian.yiffbukkit.main.util.PlayerFindException;
import de.doridian.yiffbukkit.main.util.Utils;
import de.doridian.yiffbukkit.warp.WarpDescriptor;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Names({ "who", "list" })
@Help("Prints user list if used without parameters or information about the specified user")
@Usage("[name]")
@Permission("yiffbukkit.who")
public class WhoCommand extends ICommand {
	@Override
	public void run(final CommandSender commandSender, String[] args, String argStr) throws PlayerFindException {
		if(args.length > 0) {
			final World world;
			if (commandSender instanceof Player)
				world = ((Player)commandSender).getWorld();
			else
				world = plugin.getOrCreateWorld("world", Environment.NORMAL);

			final Player target = playerHelper.matchPlayerSingle(args[0], false);

			PlayerHelper.sendDirectedMessage(commandSender, "Name: " + target.getName());
			PlayerHelper.sendDirectedMessage(commandSender, "Rank: " + playerHelper.getPlayerRank(target));
			PlayerHelper.sendDirectedMessage(commandSender, "NameTag: " + playerHelper.GetFullPlayerName(target));
			PlayerHelper.sendDirectedMessage(commandSender, "World: " + target.getWorld().getName());

			final int playerLevel = playerHelper.getPlayerLevel(commandSender);
			if (commandSender.hasPermission("yiffbukkit.who.lastlogout")) {
				PlayerHelper.sendDirectedMessage(commandSender, "Last logout: " + Utils.readableDate(PlayerHelper.lastLogout(target)));
			}

			if (commandSender.hasPermission("yiffbukkit.who.lastlogoutbackup")) {
				PlayerHelper.sendDirectedMessage(commandSender, "Last logout before backup: " + Utils.readableDate(PlayerHelper.lastLogoutBackup(target)));
			}

			final List<String> distances = new ArrayList<String>();

			final Vector targetPosition = target.getLocation().toVector();
			if (commandSender.hasPermission("yiffbukkit.who.position") && playerLevel >= playerHelper.getPlayerLevel(target)) {
				PlayerHelper.sendDirectedMessage(commandSender, "Position: " + targetPosition);

				final Vector offsetFromSpawn = targetPosition.clone().subtract(world.getSpawnLocation().toVector());
				final long unitsFromSpawn = Math.round(offsetFromSpawn.length());
				final String directionFromSpawn = Utils.yawToDirection(Utils.vectorToYaw(offsetFromSpawn));
				distances.add(unitsFromSpawn+"m "+directionFromSpawn+" from the spawn");

				if (commandSender instanceof Player) {
					final Vector offsetFromYou = targetPosition.clone().subtract(((Player)commandSender).getLocation().toVector());
					final long unitsFromYou = Math.round(offsetFromYou.length());
					final String directionFromYou = Utils.yawToDirection(Utils.vectorToYaw(offsetFromYou));
					distances.add(unitsFromYou+"m "+directionFromYou+" from you");
				}
			}

			if (commandSender.hasPermission("yiffbukkit.who.warp") && playerLevel >= playerHelper.getPlayerLevel(target)) {
				double minDistance = Double.MAX_VALUE;
				Vector minOffsetFromWarp = null;
				WarpDescriptor closestWarp = null;

				for (WarpDescriptor warpDescriptor : plugin.warpEngine.getWarps().values()) {
					final Vector currentOffsetFromWarp = targetPosition.clone().subtract(warpDescriptor.location.toVector());
					final double currentDistance = currentOffsetFromWarp.length();
					if (currentDistance >= minDistance)
						continue;

					minDistance = currentDistance;
					minOffsetFromWarp = currentOffsetFromWarp;
					closestWarp = warpDescriptor;
				}

				if (closestWarp != null) {
					final long unitsFromWarp = Math.round(minOffsetFromWarp.length());
					final String directionFromWarp = Utils.yawToDirection(Utils.vectorToYaw(minOffsetFromWarp));
					distances.add(unitsFromWarp+"m "+directionFromWarp+" from the warp \u00a79"+closestWarp.name+"\u00a7r");
				}
			}

			if (!distances.isEmpty()) {
				final StringBuilder sb = Utils.enumerateStrings(distances);
				sb.insert(0, "That's ");
				sb.append('.');
				PlayerHelper.sendDirectedMessage(commandSender, sb.toString());
			}


			if (commandSender.hasPermission("yiffbukkit.who.address") && playerLevel >= playerHelper.getPlayerLevel(target) && target.isOnline()) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						InetAddress address = target.getAddress().getAddress();
						PlayerHelper.sendDirectedMessage(commandSender, "IP: " + address.getHostAddress() + "(" + address.getCanonicalHostName() + ")");
					}
				});
				thread.start();
			}
		}
		else {
			Player[] players = plugin.getServer().getOnlinePlayers();
			String str = "Online players: ";
			if(players.length > 0) {
				if (commandSender.hasPermission("yiffbukkit.who.ranklevels")) {
					str += playerHelper.formatPlayer(players[0]);
					for(int i=1;i<players.length;i++) {
						str += ", " + playerHelper.formatPlayer(players[i]);
					}
				}
				else {
					str += players[0].getName();
					for(int i=1;i<players.length;i++) {
						str += ", " + players[i].getName();
					}
				}
			}
			PlayerHelper.sendDirectedMessage(commandSender, str);
		}
	}
}
