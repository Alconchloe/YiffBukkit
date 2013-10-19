package de.doridian.yiffbukkit.permissions;

import de.doridian.yiffbukkit.permissions.listeners.PermissionPlayerListener;
import de.doridian.yiffbukkitsplit.YiffBukkit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

public class YiffBukkitPermissions {
	public static void init() {
		new PermissionPlayerListener();

		try {
			final File file = new File(YiffBukkit.instance.getDataFolder(), "coplayers.txt");
			if (!file.exists())
				return;

			checkOffPlayers.clear();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine()) != null) {
				final String playerName = line.toLowerCase();
				checkOffPlayers.add(playerName);
				refreshCOPlayerOnlineState(playerName);
			}
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<String> checkOffPlayers = new LinkedHashSet<>();

	public static boolean addCOPlayer(Player player) {
		return addCOPlayer(player.getName());
	}
	public static boolean addCOPlayer(String playerName) {
		playerName = playerName.toLowerCase();
		if(checkOffPlayers.contains(playerName))
			return false;

		checkOffPlayers.add(playerName);
		saveCO();

		refreshCOPlayerOnlineState(playerName);

		return true;
	}
	public static boolean removeCOPlayer(Player player) {
		return removeCOPlayer(player.getName());
	}
	public static boolean removeCOPlayer(String playerName) {
		playerName = playerName.toLowerCase();
		if (!checkOffPlayers.contains(playerName))
			return false;

		checkOffPlayers.remove(playerName);
		saveCO();

		board.resetScores(getOfflinePlayer(playerName));

		return true;
	}

	private static void saveCO() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(new File(YiffBukkit.instance.getDataFolder(), "coplayers.txt")));
			String[] plys = checkOffPlayers.toArray(new String[checkOffPlayers.size()]);
			for(String ply : plys) {
				writer.println(ply.toLowerCase());
			}
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static final String DUMMY_CRITERION = "dummy";
	private static final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
	private static final Objective objective = board.registerNewObjective("checkoff", DUMMY_CRITERION);
	static {
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Name         off");
	}

	// CO online status update

	public static void refreshCOPlayerOnlineState(String playerName) {
		refreshCOPlayerOnlineState(playerName, Bukkit.getOfflinePlayer(playerName).isOnline());
	}

	public static void refreshCOPlayerOnlineState(String playerName, boolean online) {
		playerName = playerName.toLowerCase();

		if(!checkOffPlayers.contains(playerName))
			return;

		final Score score = objective.getScore(getOfflinePlayer(playerName));
		if (online) {
			score.setScore(1);
			score.setScore(0);
		}
		else {
			score.setScore(1);
		}
	}

	// CO display

	/**
	 * Toggle checkoff display for the specified player
	 *
	 * @param player Player to toggle for.
	 * @return new state
	 */
	public static boolean toggleDisplayCO(Player player) {
		if (isDisplayingCO(player)) {
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			return false;
		}
		else {
			player.setScoreboard(board);
			return true;
		}
	}

	public static boolean isDisplayingCO(Player player) {
		return player.getScoreboard() == board;
	}

	private static OfflinePlayer getOfflinePlayer(String playerName) {
		playerName = "\u00a7f" + playerName;
		final String text = playerName.substring(0, Math.min(16, playerName.length()));
		return Bukkit.getOfflinePlayer(text);
	}
}
