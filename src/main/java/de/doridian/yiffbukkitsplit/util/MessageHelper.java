package de.doridian.yiffbukkitsplit.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import de.doridian.yiffbukkit.main.StateContainer;
import de.doridian.yiffbukkit.main.chat.Parser;
import de.doridian.yiffbukkit.remote.YiffBukkitRemote;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static de.doridian.yiffbukkit.main.chat.Parser.escape;

public class MessageHelper extends StateContainer {
	private static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\"%3$s>%2$s</span>";

	private static final String YB_DEFAULT_COLOR = "dark_purple";
	private static final String YB_ERROR_COLOR = "dark_red";

	private static final String ONLINE_COLOR = "dark_green";
	private static final String OFFLINE_COLOR = "dark_red";

	public static String format(CommandSender commandSender) {
		return format(commandSender.getName(), commandSender);
	}

	public static String format(String name) {
		return format(name, Bukkit.getPlayerExact(name));
	}
	private static String format(String name, CommandSender commandSender) {
		final String onHover;
		final String displayName;
		if (commandSender == null) {
			onHover = "";
			displayName = name;
		}
		else {
			displayName = commandSender.getDisplayName();
			if (commandSender instanceof Player) {
				final Player player = (Player) commandSender;
				final String color = player.isOnline() ? ONLINE_COLOR : OFFLINE_COLOR;
				final String hoverText = String.format("<color name=\"%1$s\">%2$s</color>", color, name);
				onHover = " onHover=\"show_text('" + escape(hoverText) + "')\"";
			}
			else {
				onHover = "";
			}
		}
		return String.format(PLAYER_FORMAT, name, displayName, onHover);
	}

	public static void sendServerMessage(String format, Object... params) {
		sendColoredServerMessage(YB_DEFAULT_COLOR, format, params);
	}

	public static void sendColoredServerMessage(String color, String format, Object... params) {
		sendColoredServerMessage(color, Predicates.<Player>alwaysTrue(), format, params);
	}

	public static void sendServerMessage(Predicate<? super Player> predicate, String format, Object... params) {
		sendColoredServerMessage(YB_DEFAULT_COLOR, predicate, format, params);
	}

	public static void sendColoredServerMessage(String color, Predicate<? super Player> predicate, String format, Object... params) {
		format = "<color name=\"" + color + "\">[YB]</color> " + format;

		final Player[] players = Bukkit.getOnlinePlayers();
		final List<CommandSender> targetPlayers = new ArrayList<>();

		for (Player player : players) {
			if (!predicate.apply(player))
				continue;

			targetPlayers.add(player);
		}

		if (YiffBukkitRemote.currentCommandSender != null) {
			targetPlayers.add(YiffBukkitRemote.currentCommandSender);
		}

		Parser.sendToPlayers(targetPlayers, format, params);
	}

	public static void sendMessage(CommandSender commandSender, String format, Object... params) {
		sendMessage(YB_DEFAULT_COLOR, commandSender, format, params);
	}

	public static void sendErrorMessage(CommandSender commandSender, String format, Object... params) {
		sendMessage(YB_ERROR_COLOR, commandSender, format, params);
	}

	public static void sendMessage(String color, CommandSender commandSender, String format, Object... params) {
		format = "<color name=\"" + color + "\">[YB]</color> " + format;

		Parser.sendToPlayer(commandSender, format, params);
	}
}
