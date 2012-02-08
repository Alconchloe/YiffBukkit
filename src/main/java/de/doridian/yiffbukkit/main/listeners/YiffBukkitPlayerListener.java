package de.doridian.yiffbukkit.main.listeners;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.ToolBind;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.ICommand;
import de.doridian.yiffbukkitsplit.YiffBukkit;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissibleBase;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissionHandler;
import de.doridian.yiffbukkitsplit.listeners.YiffBukkitBlockListener;
import de.doridian.yiffbukkitsplit.util.IPGeolocation;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import de.doridian.yiffbukkitsplit.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.getspout.spout.player.SpoutCraftPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle events for all Player related events
 * @author Doridian
 */
public class YiffBukkitPlayerListener implements Listener {
	public final YiffBukkit plugin;
	private final PlayerHelper playerHelper;
	private YiffBukkitPermissionHandler permissionHandler;

	public YiffBukkitPlayerListener(YiffBukkit plug) {
		plugin = plug;
		plugin.playerListener = this;
		playerHelper = plugin.playerHelper;
		permissionHandler = plugin.permissionHandler;

		scanCommands();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		IPGeolocation.initialize();
	}

	public void scanCommands() {
		commands.clear();

		for (Class<? extends ICommand> commandClass : getSubClasses(ICommand.class)) {
			try {
				commandClass.newInstance();
			}
			catch (InstantiationException e) {
				// We try to instantiate an interface
				// or an object that does not have a 
				// default constructor
				continue;
			}
			catch (IllegalAccessException e) {
				// The class/ctor is not public
				continue;
			}
			catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private static <T> List<Class<? extends T>> getSubClasses(Class<T> baseClass) {
		final List<Class<? extends T>> ret = new ArrayList<Class<? extends T>>();
		final File file;
		try {
			final ProtectionDomain protectionDomain = baseClass.getProtectionDomain();
			final CodeSource codeSource = protectionDomain.getCodeSource();
			final URL location = codeSource.getLocation();
			final URI uri = location.toURI();
			file = new File(uri);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			return ret;
		}
		final String[] fileList;

		String packageName = baseClass.getPackage().getName();
		if (file.isDirectory() || (file.isFile() && !file.getName().endsWith(".jar"))) {
			String packageFolderName = "/"+packageName.replace('.','/');

			URL url = baseClass.getResource(packageFolderName);
			if (url == null)
				return ret;

			File directory = new File(url.getFile());
			if (!directory.exists())
				return ret;

			// Get the list of the files contained in the package
			fileList = directory.list();
		}
		else if (file.isFile()) {
			final List<String> tmp = new ArrayList<String>();
			final JarFile jarFile;
			try {
				jarFile = new JarFile(file);
			}
			catch (IOException e) {
				e.printStackTrace();
				return ret;
			}

			Pattern pathPattern = Pattern.compile(packageName.replace('.','/')+"/(.+\\.class)");
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				Matcher matcher = pathPattern.matcher(entries.nextElement().getName());
				if (!matcher.matches())
					continue;

				tmp.add(matcher.group(1));
			}

			fileList = tmp.toArray(new String[tmp.size()]);
		}
		else {
			return ret;
		}

		Pattern classFilePattern = Pattern.compile("(.+)\\.class");
		for (String fileName : fileList) {
			// we are only interested in .class files
			Matcher matcher = classFilePattern.matcher(fileName);
			if (!matcher.matches())
				continue;

			// removes the .class extension
			String classname = matcher.group(1);
			try {
				final Class<?> classObject = Class.forName(packageName+"."+classname.replace('/', '.'));
				final Class<? extends T> classT = classObject.asSubclass(baseClass);

				// Try to create an instance of the object
				ret.add(classT);
			}
			catch (ClassNotFoundException e) {
				System.err.println(e);
			}
			catch (ClassCastException e) {
				continue;
			}
		}

		return ret;
	}

	public void registerCommand(String name, ICommand command) {
		commands.put(name, command);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		final String playerName = event.getPlayer().getName();
		if (!playerName.matches("^.*[A-Za-z].*$")) {
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "[YB] Sorry, get some letters into your name.");
			return;
		}

		if (plugin.serverClosed && playerHelper.getPlayerRank(event.getPlayer()).equals("guest")) {
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "[YB] Sorry, we're closed for guests right now");
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (player instanceof SpoutCraftPlayer) {
			final SpoutCraftPlayer spoutPlayer = (SpoutCraftPlayer)player;
			Utils.setPrivateValue(SpoutCraftPlayer.class, spoutPlayer, "perm", new YiffBukkitPermissibleBase(player));
		} else {
			final CraftPlayer craftPlayer = (CraftPlayer)player;
			Utils.setPrivateValue(CraftPlayer.class, craftPlayer, "perm", new YiffBukkitPermissibleBase(player));
		}

		playerHelper.setYiffcraftState(player, false);
		playerHelper.pushPlayerLocationOntoTeleportStack(player);

		String nick = playerHelper.getPlayerNick(player.getName());
		if (nick == null)
			nick = player.getName();
		player.setDisplayName(nick);

		playerHelper.setPlayerListName(player);

		final File playerFile = PlayerHelper.getPlayerFile(player.getName(), "world");
		plugin.chatManager.pushCurrentOrigin(player);
		if (playerFile != null && playerFile.exists()) {
			plugin.ircbot.sendToPublicChannel(player.getName() + " joined!");
			plugin.ircbot.sendToStaffChannel(player.getName() + " joined with the IP " + player.getAddress().toString() + "!");
			event.setJoinMessage("\u00a72[+] \u00a7e" + playerHelper.GetFullPlayerName(player) + "\u00a7e joined from " + IPGeolocation.getCountry(player.getAddress()) + "!");
		} else {
			Location location = playerHelper.getPlayerSpawnPosition(player);
			player.teleport(location);
			plugin.ircbot.sendToPublicChannel(player.getName() + " joined for the first time!");
			plugin.ircbot.sendToStaffChannel(player.getName() + " joined with the IP " + player.getAddress().toString() + " for the first time!");
			event.setJoinMessage("\u00a72[+] \u00a7e" + playerHelper.GetFullPlayerName(player) + "\u00a7e joined from " + IPGeolocation.getCountry(player.getAddress()) + " for the first time!");
		}

		playerHelper.updateToolMappings(player);
		plugin.chatManager.popCurrentOrigin();
		playerHelper.pushWeather(player);
	}

	public Hashtable<String,String> offlinePlayers = new Hashtable<String, String>();
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final String playerName = player.getName();

		playerHelper.teleportHistory.remove(playerName.toLowerCase());

		plugin.chatManager.pushCurrentOrigin(player);
		plugin.ircbot.sendToChannel(playerName + " disconnected!");
		event.setQuitMessage("\u00a74[-] \u00a7e" + playerHelper.GetFullPlayerName(player) + "\u00a7e disconnected!");
		plugin.chatManager.popCurrentOrigin();

		offlinePlayers.put(player.getAddress().getAddress().getHostAddress(), playerName);

		for (Map<Player, ?> map : playerHelper.registeredMaps)
			map.remove(player);
		for (Set<Player> set : playerHelper.registeredSets)
			set.remove(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event) {
		if(event.isCancelled())
			return;

		final Player player = event.getPlayer();

		plugin.chatManager.pushCurrentOrigin(player);
		plugin.ircbot.sendToChannel(player.getName() + " was kicked (" + event.getReason() + ")!");
		event.setLeaveMessage("\u00a74[-] \u00a7e" + playerHelper.GetFullPlayerName(player) + "\u00a7e was kicked (" + event.getReason() + ")!");
		plugin.chatManager.popCurrentOrigin();

		for (Map<Player, ?> map : playerHelper.registeredMaps)
			map.remove(player);
		for (Set<Player> set : playerHelper.registeredSets)
			set.remove(player);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player ply = event.getPlayer();
		playerHelper.pushPlayerLocationOntoTeleportStack(ply);
		Location location = playerHelper.getPlayerSpawnPosition(ply);
		event.setRespawnLocation(location);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.isCancelled())
			return;

		event.setFormat(playerHelper.getPlayerTag(event.getPlayer()) + "%s:\u00a7f %s");

		final Player ply = event.getPlayer();
		String conversationTarget = playerHelper.conversations.get(ply.getName());
		String message = event.getMessage();
		String formattedMessage = String.format(event.getFormat(), ply.getDisplayName(), message);
		if (conversationTarget != null) {
			formattedMessage = "\u00a7e[CONV]\u00a7f "+formattedMessage;

			plugin.chatManager.pushCurrentOrigin(ply);
			ply.sendMessage(formattedMessage);
			plugin.getServer().getPlayer(conversationTarget).sendMessage(formattedMessage);
			plugin.chatManager.popCurrentOrigin();

			event.setCancelled(true);
		}
		else if(message.charAt(0) == '#') {
			event.setCancelled(true);
			plugin.ircbot.sendToStaffChannel("[OP] [" + event.getPlayer().getName() + "]: " + message.substring(1));
			message = "\u00a7e[#OP] \u00a7f" + ply.getDisplayName() + "\u00a7f: " + message.substring(1);
			playerHelper.broadcastMessage(message, "yiffbukkitsplit.opchat");

			event.setCancelled(true);
		}
	}

	public Hashtable<String,ICommand> commands = new Hashtable<String,ICommand>();
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;

		final Player ply = event.getPlayer();
		plugin.chatManager.pushCurrentOrigin(ply);
		final String cmdString = event.getMessage().substring(1).trim();
		
		if (runCommand(ply, cmdString)) {
			event.setCancelled(true);
			event.setMessage("/youdontwantthiscommand "+event.getMessage());
		}
		else
		{
			if(!(plugin.permissionHandler.has(ply, "yiffbukkitsplit.ignoreworldeditlogging") && event.getMessage().startsWith("//")))
				plugin.ircbot.sendToStaffChannel("Other Command: " + ply.getName() + ": " +cmdString);
			plugin.log("Other Command: "+ply.getName()+": "+cmdString);
		}
		plugin.chatManager.popCurrentOrigin();
	}

	public boolean runCommand(CommandSender commandSender, String cmd, String argStr) {
		String args[];
		if(argStr.isEmpty()) {
			args = new String[0];
		} else {
			args = argStr.split(" +");
		}
		return runCommand(commandSender, cmd, args, argStr);
	}

	public boolean runCommand(CommandSender commandSender, String cmd, String[] args, String argStr) {
		if (commands.containsKey(cmd)) {
			ICommand icmd = commands.get(cmd);
			try {
				if(!icmd.canPlayerUseCommand(commandSender)) {
					throw new PermissionDeniedException();
				}
				if(!(cmd.equals("msg") || cmd.equals("pm") || cmd.equals("conv") || cmd.equals("conversation")))
				{
					String logmsg = "YB Command: " + commandSender.getName() + ": "  + cmd + " " + argStr;
					plugin.ircbot.sendToStaffChannel(logmsg);
					plugin.log(logmsg);
				}
				icmd.run(commandSender, args, argStr);
			}
			catch (YiffBukkitCommandException e) {
				playerHelper.sendDirectedMessage(commandSender,e.getMessage(), e.getColor());
			}
			catch (Exception e) {
				if (plugin.permissionHandler.has(commandSender, "yiffbukkitsplit.detailederrors")) {
					playerHelper.sendDirectedMessage(commandSender,"Command error: "+e+" in "+e.getStackTrace()[0]);
					e.printStackTrace();
				}
				else {
					playerHelper.sendDirectedMessage(commandSender,"Command error!");
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean runCommand(CommandSender commandSender, String baseCmd) {
		int posSpace = baseCmd.indexOf(' ');
		String cmd; String argStr;
		if (posSpace < 0) {
			cmd = baseCmd.toLowerCase();
			argStr = "";
		} else {
			cmd = baseCmd.substring(0, posSpace).trim().toLowerCase();
			argStr = baseCmd.substring(posSpace).trim();
		}
		return runCommand(commandSender, cmd, argStr);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		/*if (event.isCancelled())
			return;*/

		Player ply = event.getPlayer();
		Block clickedBlock = event.getClickedBlock();
		switch (event.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			try {
				Material itemMaterial = event.getMaterial();
				// This will not be logged by logblock so I only allowed it for ops+ for now.
				// A fix would be to modify the event a bit to make BB log this. 
				if (itemMaterial == Material.INK_SACK && plugin.permissionHandler.has(ply, "yiffbukkitsplit.dyepaint")) {
					if (clickedBlock != null && clickedBlock.getType() == Material.WOOL) {
						ItemStack item = event.getItem();

						final byte newData = (byte)(15 - item.getDurability());

						if (plugin.logBlockConsumer != null)
							plugin.logBlockConsumer.queueBlockReplace(event.getPlayer().getName(), event.getClickedBlock().getState(), 35, newData);
						clickedBlock.setData(newData);

						int newAmount = item.getAmount()-1;
						if (newAmount > 0)
							item.setAmount(newAmount);
						else
							ply.setItemInHand(null);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Material itemMaterial = event.getMaterial();

				String key = ply.getName()+" "+itemMaterial.name();
				ToolBind toolBind = playerHelper.toolMappings.get(key);
				if (toolBind != null) {
					event.setCancelled(true);
					try {
						toolBind.run(event);
					}
					catch (YiffBukkitCommandException e) {
						playerHelper.sendDirectedMessage(ply,e.getMessage(), e.getColor());
					}
					catch (Exception e) {
						if (plugin.permissionHandler.has(ply, "yiffbukkitsplit.detailederrors")) {
							playerHelper.sendDirectedMessage(ply,"Command error: "+e+" in "+e.getStackTrace()[0]);
							e.printStackTrace();
						}
						else {
							playerHelper.sendDirectedMessage(ply,"Command error!");
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			break;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player ply = event.getPlayer();

		try {
			Material itemMaterial = ply.getItemInHand().getType();

			String key = ply.getName()+" "+itemMaterial.name();
			ToolBind toolBind = playerHelper.toolMappings.get(key);
			if (toolBind != null) {
				event.setCancelled(true);
				try {
					toolBind.run(event);
				}
				catch (YiffBukkitCommandException e) {
					playerHelper.sendDirectedMessage(ply,e.getMessage(), e.getColor());
				}
				catch (Exception e) {
					if (plugin.permissionHandler.has(ply, "yiffbukkitsplit.detailederrors")) {
						playerHelper.sendDirectedMessage(ply,"Command error: "+e+" in "+e.getStackTrace()[0]);
						e.printStackTrace();
					}
					else {
						playerHelper.sendDirectedMessage(ply,"Command error!");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		ItemStack item = event.getItemStack();
		Material itemMaterial = item.getType();
		if(itemMaterial == Material.AIR) return;

		Player ply = event.getPlayer();
		if(playerHelper.isPlayerDisabled(ply)) {
			item.setType(Material.GOLD_HOE);
			item.setAmount(1);
			item.setDurability(Short.MAX_VALUE);
			return;
		}

		if (!permissionHandler.has(ply, "yiffbukkitsplit.place")) {
			plugin.ircbot.sendToStaffChannel(ply.getName() + " is not allowed to build but tried tried to spawn " + itemMaterial+".");
			playerHelper.sendServerMessage(ply.getName() + " is not allowed to build but tried tried to spawn " + itemMaterial+".");
			item.setType(Material.GOLD_HOE);
			item.setAmount(1);
			item.setDurability(Short.MAX_VALUE);
			return;
		}

		final String permission = YiffBukkitBlockListener.blocklevels.get(itemMaterial);
		if (permission != null && !permissionHandler.has(ply, permission)) {
			plugin.ircbot.sendToStaffChannel(ply.getName() + " tried to spawn illegal block " + itemMaterial);
			playerHelper.sendServerMessage(ply.getName() + " tried to spawn illegal block " + itemMaterial);
			item.setType(Material.GOLD_HOE);
			item.setAmount(1);
			item.setDurability(Short.MAX_VALUE);
			return;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled())
			return;

		final Item item = event.getItemDrop();
		final ItemStack itemStack = item.getItemStack();
		final int typeId = itemStack.getTypeId();

		int amount = itemStack.getAmount();

		final List<Entity> nearbyEntities = item.getNearbyEntities(2, 2, 2);
		if (nearbyEntities.size() < 15)
			return;

		for (Entity entity : nearbyEntities) {
			if (!(entity instanceof Item))
				continue;

			final Item otherItem = (Item) entity;

			if (otherItem.isDead())
				continue;

			final ItemStack otherItemStack = otherItem.getItemStack();
			if (typeId != otherItemStack.getTypeId())
				continue;

			amount += otherItemStack.getAmount();

			otherItem.remove();
		}

		itemStack.setAmount(amount);
	}
}
