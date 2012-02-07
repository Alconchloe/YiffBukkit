package de.doridian.yiffbukkit;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.doridian.yiffbukkit.chat.ChatListener;
import de.doridian.yiffbukkit.chat.manager.ChatManager;
import de.doridian.yiffbukkit.commands.ICommand;
import de.doridian.yiffbukkit.console.YiffBukkitConsoleCommands;
import de.doridian.yiffbukkit.irc.Ircbot;
import de.doridian.yiffbukkit.jail.JailEngine;
import de.doridian.yiffbukkit.listeners.SignPortalPlayerListener;
import de.doridian.yiffbukkit.listeners.YiffBukkitBlockListener;
import de.doridian.yiffbukkit.listeners.YiffBukkitEntityListener;
import de.doridian.yiffbukkit.listeners.YiffBukkitPacketListener;
import de.doridian.yiffbukkit.listeners.YiffBukkitPlayerListener;
import de.doridian.yiffbukkit.listeners.YiffBukkitVehicleListener;
import de.doridian.yiffbukkit.mcbans.MCBans;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissionHandler;
import de.doridian.yiffbukkit.portals.PortalEngine;
import de.doridian.yiffbukkit.remote.YiffBukkitRemote;
import de.doridian.yiffbukkit.ssl.SSLUtils;
import de.doridian.yiffbukkit.ssl.ServerSSLSocket;
import de.doridian.yiffbukkit.transmute.Transmute;
import de.doridian.yiffbukkit.util.Configuration;
import de.doridian.yiffbukkit.util.PlayerHelper;
import de.doridian.yiffbukkit.util.SpawnUtils;
import de.doridian.yiffbukkit.util.Utils;
import de.doridian.yiffbukkit.vanish.Vanish;
import de.doridian.yiffbukkit.warp.WarpEngine;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.dynmap.ChatEvent;
import org.dynmap.DynmapPlugin;
import org.dynmap.Event;
import org.dynmap.Event.Listener;
import org.dynmap.SimpleWebChatComponent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * YiffBukkit
 * @author Doridian
 */
public class YiffBukkit extends JavaPlugin {
	public static YiffBukkit instance;
	public YiffBukkitPlayerListener playerListener;
	@SuppressWarnings("unused")
	private YiffBukkitBlockListener blockListener;
	@SuppressWarnings("unused")
	private YiffBukkitPacketListener yiffBukkitPacketListener;
	@SuppressWarnings("unused")
	private YiffBukkitEntityListener yiffBukkitEntityListener;
	@SuppressWarnings("unused")
	private YiffBukkitVehicleListener yiffBukkitVehicleListener;
	@SuppressWarnings("unused")
	private SignPortalPlayerListener signPortalPlayerListener;
	@SuppressWarnings("unused")
	private ChatListener chatListener;
	@SuppressWarnings("unused")
	private YiffBukkitConsoleCommands consoleCommands;

	public Vanish vanish;
	public Transmute transmute;
	private YiffBukkitRemote remote;
	public PlayerHelper playerHelper = null;
	public final Utils utils = new Utils(this);
	public final SpawnUtils spawnUtils = new SpawnUtils(this);
	public WarpEngine warpEngine;
	public JailEngine jailEngine;
	public SignSaver signSaver;
	public PortalEngine portalEngine;
	public ChatManager chatManager;

	public Permissions permissions;
	public YiffBukkitPermissionHandler permissionHandler;
	public MCBans mcbans;
	public Ircbot ircbot;
	public WorldEditPlugin worldEdit;
	public DynmapPlugin dynmap;
	public Consumer logBlockConsumer;

	public ServerSSLSocket serverSSLSocket;

	public boolean serverClosed = false;

	public YiffBukkit() {
		instance = this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onLoad() {
		final SimplePluginManager pm = (SimplePluginManager)getServer().getPluginManager();
		permissions = new Permissions(this,this.getClassLoader(),this.getFile());

		((List<Plugin>)Utils.getPrivateValue(SimplePluginManager.class, pm, "plugins")).add(permissions);
		((HashMap<String,Plugin>)Utils.getPrivateValue(SimplePluginManager.class, pm, "lookupNames")).put("Permissions", permissions);

		log( "Started YiffBukkitPermissions!" );
		permissionHandler = (YiffBukkitPermissionHandler)permissions.getHandler();
	}

	public void onDisable() {
		if (serverSSLSocket != null)
			serverSSLSocket.stopme();
		remote.stopme();
		log("Plugin disabled!" ) ;
	}

	public void setupIPC() {
		final PluginManager pm = getServer().getPluginManager();

		worldEdit = (WorldEditPlugin) pm.getPlugin("WorldEdit");
		if (worldEdit != null)
			log( "Found WorldEdit!" );

		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				try {
					Plugin tmp = getServer().getPluginManager().getPlugin("dynmap");
					if (tmp == null)
						return;
					dynmap = (DynmapPlugin)tmp;

					Event<?> event = dynmap.events.events.get("webchat");

					// listeners = event.listeners;
					List<Event.Listener<ChatEvent>> listeners = Utils.getPrivateValue(Event.class, event, "listeners");
					if (listeners == null)
						return;

					// Remove the old listener
					for (Iterator<Listener<ChatEvent>> it = listeners.iterator(); it.hasNext(); ) {
						Listener<ChatEvent> foo = it.next();
						if (!foo.getClass().getEnclosingClass().equals(SimpleWebChatComponent.class))
							continue;

						it.remove();
					}

					listeners.add(new Listener<ChatEvent>() {
						@Override
						public void triggered(ChatEvent t) {
							String name = t.name.replace('\u00a7','$');
							name = playerHelper.getPlayerNameByIP(name);
							ircbot.sendToChannel("[WEB] " + name + ": " + t.message.replace('\u00a7','$'));
							getServer().broadcast(Server.BROADCAST_CHANNEL_USERS, "[WEB]" + name + ": " + t.message.replace('\u00a7','$'));
						}
					});
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});

		LogBlock logBlock = (LogBlock) pm.getPlugin("LogBlock");

		if (logBlock != null) {
			logBlockConsumer = logBlock.getConsumer();
			log( "Found LogBlock!" );
		}
	}

	public void onEnable() {
		setupIPC();

		playerHelper = new PlayerHelper(this);
		warpEngine = new WarpEngine(this);
		jailEngine = new JailEngine(this);
		signSaver = new SignSaver(this);
		//portalEngine = new PortalEngine(this);
		log("State components loaded.");
		StateContainer.loadAll();
		log("State component config loaded.");
		chatManager = new ChatManager(this);

		playerListener = new YiffBukkitPlayerListener(this);
		blockListener = new YiffBukkitBlockListener(this);
		yiffBukkitPacketListener = new YiffBukkitPacketListener(this);
		yiffBukkitEntityListener = new YiffBukkitEntityListener(this);
		yiffBukkitVehicleListener = new YiffBukkitVehicleListener(this);
		signPortalPlayerListener = new SignPortalPlayerListener(this);
		vanish = new Vanish(this);
		transmute = new Transmute(this);
		chatListener = new ChatListener(this);
		consoleCommands = new YiffBukkitConsoleCommands(this);

		log("Core components loaded.");
		mcbans = new MCBans(this);
		log("MCBans loaded.");
		ircbot = new Ircbot(this).init();
		log("IRC bot loaded.");

		remote = new YiffBukkitRemote(this, playerListener);
		remote.start();
		log("Remote loaded.");

		try {
			serverSSLSocket = new ServerSSLSocket(this);
			serverSSLSocket.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				List<LivingEntity> removeList = new ArrayList<LivingEntity >(); 
				for (World world : getServer().getWorlds()) {
					for (LivingEntity livingEntity : world.getLivingEntities()) {
						if (livingEntity instanceof Slime) {
							removeList.add(livingEntity);
						}
					}
				}

				for (LivingEntity livingEntity : removeList) {
					livingEntity.remove();
				}
			}
		}, 1000, 200);

		log( "Plugin enabled!" );

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "yiffcraft");
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "yiffcraft", new PluginMessageListener() {
			@Override
			public void onPluginMessageReceived(String s, Player ply, byte[] bytes) {
				String argStr = new String(bytes);

				playerHelper.setYiffcraftState(ply, true);
				SSLUtils.nagIfNoSSL(playerHelper, ply);

				if(argStr.equalsIgnoreCase("getcommands")) {
					playerHelper.sendYiffcraftClientCommand(ply, 'c', Configuration.getValue("yiffcraft-command-url", "http://commands.yiffcraft.net/servers/mc_doridian_de.txt"));
				} else if(argStr.equalsIgnoreCase("writecommands")) {
					try {
						Hashtable<String, ICommand> commands = playerListener.commands;

						PrintWriter writer = new PrintWriter(new FileWriter("yb_commands.txt"));

						for(Map.Entry<String, ICommand> command : commands.entrySet()) {
							ICommand cmd = command.getValue();
							String help = cmd.getHelp();
							if(help.indexOf("\n") > 0) {
								help = help.substring(0, help.indexOf("\n"));
							}
							writer.println('/' + command.getKey() + '|' + cmd.getUsage() + " - " + help);
						}

						writer.close();
					}
					catch(Exception e) { playerHelper.sendDirectedMessage(ply, "Error: " + e.getMessage()); }
				}
			}
		});
	}
	
	public void log(String msg) {
		log(Level.INFO, msg);
	}

	public void log(Level level, String msg) {
		getLogger().log(level, msg);
	}
	
	public void sendConsoleMsg(String msg) {
		sendConsoleMsg(msg, true);
	}

	public void sendConsoleMsg(String msg, boolean addprefix) {
		if(addprefix) {
			msg = "\u00a7d[YB]\u00a7f " + msg;
		}
		ColouredConsoleSender.getInstance().sendMessage(msg);
	}

	public Hashtable<String,ICommand> getCommands() {
		return playerListener.commands;
	}

	public World getOrCreateWorld(String name, Environment env) {
		name = name.toLowerCase();
		/*for (World world : getServer().getWorlds()) {
			if (world.getName().equals(name)) return world;
		}*/
		return getServer().getWorld(name);
	}
}