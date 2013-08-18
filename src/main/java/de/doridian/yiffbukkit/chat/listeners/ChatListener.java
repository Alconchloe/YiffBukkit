package de.doridian.yiffbukkit.chat.listeners;

import de.doridian.yiffbukkit.chat.ChatHelper;
import de.doridian.yiffbukkit.chat.ChatSounds;
import de.doridian.yiffbukkit.chat.RedisHandler;
import de.doridian.yiffbukkit.main.listeners.BaseListener;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener extends BaseListener {
	private final ChatHelper helper;
	//ChatScreenListener screen;

	public ChatListener() {
		helper = new ChatHelper(plugin);
		RedisHandler.initialize();
		//screen = new ChatScreenListener(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;

		String msg = event.getMessage();
		char fchar = msg.charAt(0);
		if (fchar == '/' || fchar == '#')
			return;

		ChatSounds.processMessage(event.getPlayer(), msg);

		try {
			helper.sendChat(event.getPlayer(), msg, true);
		}
		catch (Exception e) {
			PlayerHelper.sendDirectedMessage(event.getPlayer(), e.getMessage());
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player ply = event.getPlayer();
		helper.verifyPlayerInDefaultChannel(ply);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {

	}
}
