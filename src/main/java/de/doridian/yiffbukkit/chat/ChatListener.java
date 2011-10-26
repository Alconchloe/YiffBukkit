package de.doridian.yiffbukkit.chat;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

import de.doridian.yiffbukkit.YiffBukkit;

public class ChatListener extends PlayerListener {
	ChatHelper helper;
	YiffBukkit plugin;
	
	public ChatListener(YiffBukkit plug) {
		plugin = plug;
		helper = new ChatHelper(plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, this, Priority.Highest, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if(event.isCancelled()) return;
		
		String msg = event.getMessage();
		char fchar = msg.charAt(0);
		if(fchar == '/' || fchar == '#') return;
		
		helper.sendChat(event.getPlayer(), msg, true);
		
		event.setCancelled(true);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		helper.verifyPlayerInDefaultChannel(event.getPlayer());
	}
}
