package de.doridian.yiffbukkit.chat;

import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.util.PlayerNotFoundException;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class ChatChannel implements Serializable {
	private static final long serialVersionUID = 1L;

	public ChatChannelMode mode = ChatChannelMode.PUBLIC;
	public final String name;
	public String password = "";

	public final HashSet<String> users = new HashSet<>();
	public final HashSet<String> moderators = new HashSet<>();
	public String owner;

	public int range = 0;

	public final HashMap<String,Boolean> players = new HashMap<>();

	public ChatChannel(String name) {
		this.name = name;
	}

	public boolean canJoin(Player ply) {
		return canJoin(ply, "");
	}

	public boolean canJoin(Player ply, String pass) {
		if (ply.hasPermission("yiffbukkit.channels.force.user"))
			return true;

		final String playerName = ply.getName().toLowerCase();

		if (users.contains(playerName))
			return true;

		if (moderators.contains(playerName))
			return true;

		if (mode == ChatChannelMode.PRIVATE)
			return false;

		if (password.isEmpty())
			return true;

		//noinspection RedundantIfStatement
		if (pass.equals(password))
			return true;

		return false;
	}

	public boolean canHear(Player target, Player source) {
		if (source == null)
			return true;

		if (target == source)
			return true;

		final String targetName = target.getName().toLowerCase();

		//is the player in the channel?
		if (!players.containsKey(targetName))
			return false;

		//is the player listening to the channel?
		if (!players.get(targetName))
			return false;

		//is the player in range of the channel?
		if (range <= 0)
			return true;

		if (target.getWorld() != source.getWorld())
			return false;

		//noinspection RedundantIfStatement
		if (target.getLocation().distance(source.getLocation()) > range)
			return false;

		return true;

	}

	public boolean canSpeak(Player player) {
		if (player == null)
			return true;

		final String playerName = player.getName().toLowerCase();

		//is the player in the channel?
		if (!players.containsKey(playerName))
			return false;

		//if channel is moderated, is user in the users list?
		if (mode != ChatChannelMode.MODERATED)
			return true;

		//noinspection RedundantIfStatement
		if (isUser(player))
			return true;

		return false;

	}

	public void addUser(Player player) throws YiffBukkitCommandException {
		if (player == null)
			throw new PlayerNotFoundException();

		final String playerName = player.getName().toLowerCase();
		if (this.users.contains(playerName))
			throw new YiffBukkitCommandException("Player is already a user of this channel!");

		this.users.add(playerName);
	}

	public void removeUser(Player player) throws YiffBukkitCommandException {
		if (player == null)
			throw new PlayerNotFoundException();

		try {
			removeModerator(player);
		}
		catch (YiffBukkitCommandException ignored) { }

		final String playerName = player.getName().toLowerCase();
		if (!this.users.contains(playerName))
			throw new YiffBukkitCommandException("Player is not a user of this channel!");

		this.users.remove(playerName);
	}

	public void addModerator(Player player) throws YiffBukkitCommandException {
		if (player == null)
			throw new PlayerNotFoundException();

		try {
			addUser(player);
		}
		catch (YiffBukkitCommandException ignored) { }

		final String playerName = player.getName().toLowerCase();
		if (this.moderators.contains(playerName))
			throw new YiffBukkitCommandException("Player is already a moderator of this channel!");

		this.moderators.add(playerName);
	}

	public void removeModerator(Player player) throws YiffBukkitCommandException {
		if (player == null)
			throw new PlayerNotFoundException();

		final String playerName = player.getName().toLowerCase();
		if (!this.moderators.contains(playerName))
			throw new YiffBukkitCommandException("Player is not a moderator of this channel!");

		this.moderators.remove(playerName);
	}

	public boolean isOwner(Player player) {
		if (player.getName().toLowerCase().equals(this.owner))
			return true;

		//noinspection RedundantIfStatement
		if (player.hasPermission("yiffbukkit.channels.force.owner"))
			return true;

		return false;
	}

	public boolean isModerator(Player player) {
		if (isOwner(player))
			return true;

		if (moderators.contains(player.getName().toLowerCase()))
			return true;

		//noinspection RedundantIfStatement
		if (player.hasPermission("yiffbukkit.channels.force.moderator"))
			return true;

		return false;
	}

	public boolean isUser(Player player) {
		if (isModerator(player))
			return true;

		//noinspection RedundantIfStatement
		if (users.contains(player.getName().toLowerCase()))
			return true;

		return false;
	}

	public enum ChatChannelMode {
		PUBLIC, PRIVATE, MODERATED
	}
}
