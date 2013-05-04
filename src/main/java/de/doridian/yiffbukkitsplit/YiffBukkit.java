package de.doridian.yiffbukkitsplit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.doridian.yiffbukkit.chat.listeners.ChatListener;
import de.doridian.yiffbukkit.chat.manager.ChatManager;
import de.doridian.yiffbukkit.componentsystem.ComponentSystem;
import de.doridian.yiffbukkit.irc.Ircbot;
import de.doridian.yiffbukkit.main.StateContainer;
import de.doridian.yiffbukkit.main.commands.system.CommandSystem;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.console.YiffBukkitConsoleCommands;
import de.doridian.yiffbukkit.main.listeners.YiffBukkitPlayerListener;
import de.doridian.yiffbukkit.main.util.Configuration;
import de.doridian.yiffbukkit.main.util.PersistentScheduler;
import de.doridian.yiffbukkit.main.util.Utils;
import de.doridian.yiffbukkit.mcbans.MCBans;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissionHandler;
import de.doridian.yiffbukkit.permissions.YiffBukkitPermissions;
import de.doridian.yiffbukkit.portal.PortalEngine;
import de.doridian.yiffbukkit.remote.YiffBukkitRemote;
import de.doridian.yiffbukkit.spawning.SpawnUtils;
import de.doridian.yiffbukkit.transmute.Transmute;
import de.doridian.yiffbukkit.warp.WarpEngine;
import de.doridian.yiffbukkit.warp.listeners.SignPortalPlayerListener;
import de.doridian.yiffbukkit.yiffpoints.YBBank;
import de.doridian.yiffbukkitsplit.listeners.YiffBukkitBlockListener;
import de.doridian.yiffbukkitsplit.listeners.YiffBukkitBungeeLink;
import de.doridian.yiffbukkitsplit.listeners.YiffBukkitEntityListener;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_5_R3.command.ColouredConsoleSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * YiffBukkit
 * @author Doridian
 */
public class YiffBukkit extends JavaPlugin {
	public static YiffBukkit instance;
	public ComponentSystem componentSystem = new ComponentSystem();
	@SuppressWarnings("unused")
	private YiffBukkitPlayerListener playerListener;
	@SuppressWarnings("unused")
	private YiffBukkitBlockListener blockListener;
	@SuppressWarnings("unused")
	private YiffBukkitEntityListener yiffBukkitEntityListener;
    @SuppressWarnings("unused")
    private YiffBukkitBungeeLink yiffBukkitBungeeLink;
	@SuppressWarnings("unused")
	private SignPortalPlayerListener signPortalPlayerListener;
	@SuppressWarnings("unused")
	private ChatListener chatListener;
	@SuppressWarnings("unused")
	private YiffBukkitConsoleCommands consoleCommands;

	public Transmute transmute;
	private YiffBukkitRemote remote;
	public final PlayerHelper playerHelper = new PlayerHelper(this);
	public final Utils utils = new Utils(this);
	public final SpawnUtils spawnUtils = new SpawnUtils(this);
	public WarpEngine warpEngine;
	public PortalEngine portalEngine;
	public ChatManager chatManager;
	public PersistentScheduler persistentScheduler;

	public MCBans mcbans;
	public Ircbot ircbot;
	public WorldEditPlugin worldEdit;
	public Consumer logBlockConsumer;

	public LockDownMode lockdownMode = LockDownMode.OFF;
	public CommandSystem commandSystem;
	public final YBBank bank = new YBBank();

	public YiffBukkit() {
		instance = this;
		componentSystem.registerComponents();
	}

	public void onDisable() {
		remote.stopme();
		log("Plugin disabled!" ) ;
	}

	public void setupIPC() {
		final PluginManager pm = getServer().getPluginManager();

		worldEdit = (WorldEditPlugin) pm.getPlugin("WorldEdit");
		if (worldEdit != null)
			log( "Found WorldEdit!" );

		LogBlock logBlock = (LogBlock) pm.getPlugin("LogBlock");

		if (logBlock != null) {
			logBlockConsumer = logBlock.getConsumer();
			log( "Found LogBlock!" );
		}
	}

	public void onEnable() {
		setupIPC();

		YiffBukkitPermissionHandler.instance.load();

		warpEngine = new WarpEngine(this);
		persistentScheduler = new PersistentScheduler();
		//portalEngine = new PortalEngine(this);
		log("State components loaded.");
		StateContainer.loadAll();
		log("State component config loaded.");
		chatManager = new ChatManager(this);

		commandSystem = new CommandSystem(this);
		componentSystem.registerCommands();
		playerListener = new YiffBukkitPlayerListener();
		blockListener = new YiffBukkitBlockListener();
		yiffBukkitEntityListener = new YiffBukkitEntityListener();
		signPortalPlayerListener = new SignPortalPlayerListener();
		transmute = new Transmute(this);
		chatListener = new ChatListener();
		consoleCommands = new YiffBukkitConsoleCommands(this);
		componentSystem.registerListeners();
        yiffBukkitBungeeLink = new YiffBukkitBungeeLink();

		log("Core components loaded.");
		mcbans = new MCBans(this);
		log("MCBans loaded.");
		ircbot = new Ircbot(this).init();
		log("IRC bot loaded.");

		remote = new YiffBukkitRemote(this);
		remote.start();
		log("Remote loaded.");

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
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "yiffcraftp");
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "yiffcraft", new PluginMessageListener() {
			@Override
			public void onPluginMessageReceived(String s, Player ply, byte[] bytes) {
				String argStr = new String(bytes);

				playerHelper.setYiffcraftState(ply, true);
				//SSLUtils.nagIfNoSSL(playerHelper, ply);

				if(argStr.equalsIgnoreCase("getcommands")) {
					playerHelper.sendYiffcraftClientCommand(ply, 'c', Configuration.getValue("yiffcraft-command-url", "http://commands.yiffcraft.net/servers/mc_doridian_de.txt"));
				} else if(argStr.equalsIgnoreCase("writecommands")) {
					try {
						Map<String, ICommand> commands = commandSystem.getCommands();

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
					catch(Exception e) { PlayerHelper.sendDirectedMessage(ply, "Error: " + e.getMessage()); }
				}
			}
		});

		YiffBukkitPermissions.init();
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

	public World getOrCreateWorld(String name) {
		name = name.toLowerCase();
		World world = getServer().getWorld(name);
		if(world == null) {
			return getServer().createWorld(WorldCreator.name(name));
		}
		return world;
	}

	public World getOrCreateWorld(String name, Environment env) {
		name = name.toLowerCase();
		World world = getServer().getWorld(name);
		if(world == null) {
			return getServer().createWorld(WorldCreator.name(name).environment(env));
		}
		return world;
	}
}
