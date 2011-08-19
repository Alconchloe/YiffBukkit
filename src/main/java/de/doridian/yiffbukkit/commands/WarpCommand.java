package de.doridian.yiffbukkit.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.warp.WarpDescriptor;
import de.doridian.yiffbukkit.warp.WarpException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("warp")
@Help("Teleports you to the specified warp point.")
@Usage("<warp point name>|+ <command>[ <args>] - see /cwarp")
@Permission("yiffbukkit.warp.warp")
public class WarpCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		String playerName = commandSender.getName();
		if (args.length == 0) {
			//warp
			final StringBuilder sb = new StringBuilder("Available warps: ");
			boolean first = true;

			final Collection<WarpDescriptor> values = plugin.warpEngine.getWarps().values();
			final WarpDescriptor[] valueArray = values.toArray(new WarpDescriptor[values.size()]);

			if (commandSender instanceof Player) {
				final Vector playerPos = ((Player)commandSender).getLocation().toVector();
				Arrays.sort(valueArray, 0, valueArray.length, new Comparator<WarpDescriptor>() {
					public int compare(WarpDescriptor lhs, WarpDescriptor rhs) {
						return -Double.compare(lhs.location.toVector().distanceSquared(playerPos), rhs.location.toVector().distanceSquared(playerPos));
					}
				});
			}

			for (WarpDescriptor warp : valueArray) {
				if (warp.isHidden)
					continue;

				final int rank = warp.checkAccess(playerName);
				if (rank < 1)
					continue;

				if (!first)
					sb.append(", ");

				if (rank == 2) // TODO: use actual rank, not checkAccess
					sb.append("�7@�f");
				else if (rank >= 2)
					sb.append("�7#�f");

				sb.append(warp.name);

				first = false;
			}

			playerHelper.sendDirectedMessage(commandSender, sb.toString());
			return;
		}
		if (args[0].equals("help")) {
			//warp help
			playerHelper.sendDirectedMessage(commandSender, "/warp <warp point name> [<command>[ <args>]]");
			playerHelper.sendDirectedMessage(commandSender, "commands:");
			playerHelper.sendDirectedMessage(commandSender, "without arguments - teleport to warp");
			playerHelper.sendDirectedMessage(commandSender, "info - Shows information");
			playerHelper.sendDirectedMessage(commandSender, "changeowner <new owner> - Transfers ownership");
			playerHelper.sendDirectedMessage(commandSender, "public|private - Change public access");
			playerHelper.sendDirectedMessage(commandSender, "hide|show - Change warp visibility in warp list");
			playerHelper.sendDirectedMessage(commandSender, "addguest <name> - Grant guest access (can teleport)");
			playerHelper.sendDirectedMessage(commandSender, "addop <name> - Grant op access (can add guests)");
			playerHelper.sendDirectedMessage(commandSender, "deny <name> - Deny access");
			playerHelper.sendDirectedMessage(commandSender, "move - Move the warp to your current position");
			playerHelper.sendDirectedMessage(commandSender, "remove - Deletes the warp. This cannot be undone!");
			return;
		}

		try {
			final WarpDescriptor warp = plugin.warpEngine.getWarp(playerName, args[0]);
			if (args.length == 1) {
				//warp <warp point name>
				if (plugin.jailEngine.isJailed(asPlayer(commandSender)))
					throw new YiffBukkitCommandException("You are jailed!");

				asPlayer(commandSender).teleport(warp.location);
				return;
			}

			final String command = args[1].toLowerCase();

			int rank = warp.checkAccess(playerName);

			if (command.equals("chown") || command.equals("changeowner")) {
				//warp <warp point name> changeowner <new owner>
				final String newOwnerName = playerHelper.completePlayerName(args[2], false);
				if (newOwnerName == null)
					throw new WarpException("No unique player found for '"+args[2]+"'");

				warp.setOwner(playerName, newOwnerName);

				playerHelper.sendDirectedMessage(commandSender, "Transferred ownership of warp �9" + warp.name + "�f to "+newOwnerName+".");
			}
			else if (command.equals("hide")) {
				//warp <warp point name> public
				if (rank < 3)
					throw new WarpException("Permission denied");

				warp.isHidden = true;

				playerHelper.sendDirectedMessage(commandSender, "Hiding warp �9" + warp.name + "�f in warp list.");
			}
			else if (command.equals("show") || command.equals("unhide")) {
				//warp <warp point name> public
				if (rank < 3)
					throw new WarpException("Permission denied");

				warp.isHidden = true;

				playerHelper.sendDirectedMessage(commandSender, "Showing warp �9" + warp.name + "�f in warp list.");
			}
			else if (command.equals("public") || command.equals("unlock")) {
				//warp <warp point name> public
				if (rank < 2)
					throw new WarpException("Permission denied");

				warp.isPublic = true;

				playerHelper.sendDirectedMessage(commandSender, "Set warp �9" + warp.name + "�f to public.");
			}
			else if (command.equals("private") || command.equals("lock")) {
				//warp <warp point name> private
				warp.isPublic = false;

				playerHelper.sendDirectedMessage(commandSender, "Set warp �9" + warp.name + "�f to private.");
			}
			else if (command.equals("deny")) {
				//warp <warp point name> deny <name>
				final String targetName = playerHelper.completePlayerName(args[2], false);
				if (targetName == null)
					throw new WarpException("No unique player found for '"+args[2]+"'");

				warp.setAccess(playerName, targetName, 0);

				playerHelper.sendDirectedMessage(commandSender, "Revoked " + targetName + "'s access to warp �9" + warp.name + "�f.");
			}
			else if (command.equals("addguest")) {
				//warp <warp point name> addguest <name>
				final String targetName = playerHelper.completePlayerName(args[2], false);
				if (targetName == null)
					throw new WarpException("No unique player found for '"+args[2]+"'");

				warp.setAccess(playerName, targetName, 1);

				playerHelper.sendDirectedMessage(commandSender, "Granted " + targetName + " guest access to warp �9" + warp.name + "�f.");
			}
			else if (command.equals("addop")) {
				//warp <warp point name> addop <name>
				final String targetName = playerHelper.completePlayerName(args[2], false);
				if (targetName == null)
					throw new WarpException("No unique player found for '"+args[2]+"'");

				warp.setAccess(playerName, targetName, 2);

				playerHelper.sendDirectedMessage(commandSender, "Granted " + targetName + " op access to warp �9" + warp.name + "�f.");
			}
			else if (command.equals("move")) {
				//warp <warp point name> move
				if (rank < 3)
					throw new WarpException("You need to be the warp's owner to do this.");

				warp.location = asPlayer(commandSender).getLocation().clone();

				playerHelper.sendDirectedMessage(commandSender, "Moved warp �9" + warp.name + "�f to your current location.");
			}
			else if (command.equals("info")) {
				//warp <warp point name> info
				final Vector warpPosition = warp.location.toVector();

				playerHelper.sendDirectedMessage(commandSender, "Warp �9" + warp.name + "�f is owned by "+warp.getOwner());
				if (warp.isPublic)
					playerHelper.sendDirectedMessage(commandSender, "Warp is public");
				else
					playerHelper.sendDirectedMessage(commandSender, "Warp is private");

				final StringBuilder sb = new StringBuilder("Access list: ");
				boolean first = true;
				for (Entry<String, Integer> entry : warp.getRanks().entrySet()) {
					if (!first)
						sb.append(", ");

					if (entry.getValue() >= 2)
						sb.append('@');

					sb.append(entry.getKey());

					first = false;
				}
				playerHelper.sendDirectedMessage(commandSender, sb.toString());

				String msg = "This warp is ";

				if (commandSender instanceof Player) {
					final long unitsFromYou = Math.round(warpPosition.distance(((Player)commandSender).getLocation().toVector()));
					msg += unitsFromYou + "m from you and ";
				}

				final long unitsFromSpawn = Math.round(warpPosition.distance(warp.location.getWorld().getSpawnLocation().toVector()));
				msg += unitsFromSpawn + "m from the spawn.";

				playerHelper.sendDirectedMessage(commandSender, msg);
			}
			else if (command.equals("remove")) {
				plugin.warpEngine.removeWarp(playerName, warp.name);
				playerHelper.sendDirectedMessage(commandSender, "Removed warp �9" + warp.name + "�f.");
			}
			else {
				throw new WarpException("Unknown /warp command.");
			}
			plugin.warpEngine.SaveWarps();
		}
		catch (ArrayIndexOutOfBoundsException e) {
			playerHelper.sendDirectedMessage(commandSender, "Not enough arguments.");
		}
	}
}
