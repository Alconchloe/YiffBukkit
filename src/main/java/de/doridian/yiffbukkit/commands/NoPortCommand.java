package de.doridian.yiffbukkit.commands;

import java.util.HashSet;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;

public class NoPortCommand extends ICommand {
	protected HashSet<String> tpPermissions;
	protected HashSet<String> summonPermissions;

	public int GetMinLevel() {
		return 1;
	}

	public NoPortCommand(YiffBukkit plug) {
		super(plug);
		tpPermissions = playerHelper.playerTpPermissions;
		summonPermissions = playerHelper.playerSummonPermissions;
	}

	public void Run(Player ply, String[] args, String argStr) {
		boolean newState;
		String playerName = ply.getName();

		String arg0 = args.length >= 1 ? args[0] : "";

		if (argStr.equals("on") || argStr.equals("1")) {
			newState = true;
		}
		else if (argStr.equals("off") || argStr.equals("0")) {
			newState = false;
		}
		else if (arg0.equals("allow") || arg0.equals("accept")) {
			if (args.length < 2) {
				playerHelper.SendDirectedMessage(ply, "Usage: " + GetUsage());
				return;
			}

			String otherName = playerHelper.CompletePlayerName(args[1], true);
			if (otherName == null) {
				playerHelper.SendDirectedMessage(ply, "Sorry, multiple players found!");
			}
			else {
				setException(playerName, otherName, true);
				playerHelper.SendDirectedMessage(ply, "Allowed "+what()+" for "+otherName+".");
			}
			return;
		}
		else if (arg0.equals("deny") || arg0.equals("reject") || arg0.equals("revoke") || arg0.equals("forbid")) {
			if (args.length < 2) {
				playerHelper.SendDirectedMessage(ply, "Usage: " + GetUsage());
				return;
			}

			String otherName = playerHelper.CompletePlayerName(args[1], true);
			if (otherName == null) {
				playerHelper.SendDirectedMessage(ply, "Sorry, multiple players found!");
			}
			else {
				setException(playerName, otherName, false);
				playerHelper.SendDirectedMessage(ply, "Disallowed "+what()+" for "+otherName+".");
			}
			return;
		}
		else if (argStr.isEmpty()) {
			// toggle
			if (tpPermissions == null) {
				newState = !summonPermissions.contains(playerName);
			}
			else if (summonPermissions == null || tpPermissions.contains(playerName) == summonPermissions.contains(playerName)) {
				newState = !tpPermissions.contains(playerName);
			}
			else {
				playerHelper.SendDirectedMessage(ply, "The states of notp and nosummon differ. Please use !noport on/off explicitly.");
				return;
			}
		}
		else {
			playerHelper.SendDirectedMessage(ply, "Usage: " + GetUsage());
			return;
		}

		if (tpPermissions != null) {
			if (newState)
				tpPermissions.add(playerName);
			else
				tpPermissions.remove(playerName);
		}

		if (summonPermissions != null) {
			if (newState)
				summonPermissions.add(playerName);
			else
				summonPermissions.remove(playerName);
		}
		playerHelper.SavePortPermissions();

		playerHelper.SendDirectedMessage(ply, (newState ? "Disallowed" : "Allowed")+" "+what()+".");
	}

	private void setException(String playerName, String otherName, boolean newState) {
		String pair = playerName+" "+otherName;

		if (tpPermissions != null) {
			if (newState)
				tpPermissions.add(pair);
			else
				tpPermissions.remove(pair);
		}
		if (summonPermissions != null) {
			if (newState)
				summonPermissions.add(pair);
			else
				summonPermissions.remove(pair);
		}
		playerHelper.SavePortPermissions();
	}

	protected String what() {
		return "teleportation and summoning";
	}

	public String GetHelp() {
		return "Prevents "+what()+" or grants/revokes exceptions.";
	}

	public String GetUsage() {
		return "[on|off|allow <name>|deny <name>]";
	}
}
