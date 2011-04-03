package de.doridian.yiffbukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.doridian.yiffbukkit.commands.*;
import de.doridian.yiffbukkit.util.PlayerHelper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

/**
 * Handle events for all Player related events
 * @author Doridian
 */
public class YiffBukkitPlayerListener extends PlayerListener {
	private final YiffBukkit plugin;
	private final PlayerHelper playerHelper;

	public YiffBukkitPlayerListener(YiffBukkit instance) {
		plugin = instance;
		playerHelper = plugin.playerHelper;

		commands.put("me", new MeCommand(plugin));
		commands.put("pm", new PmCommand(plugin));

		commands.put("who", new WhoCommand(plugin));
		commands.put("help", new HelpCommand(plugin));

		commands.put("setrank", new SetRankCommand(plugin));
		commands.put("settag", new SetTagCommand(plugin));
		commands.put("setnick", new SetNickCommand(plugin));

		commands.put("kick", new KickCommand(plugin));
		commands.put("ban", new BanCommand(plugin));
		commands.put("unban", new UnbanCommand(plugin));
		commands.put("pardon", new UnbanCommand(plugin));
		commands.put("kickall", new KickAllCommand(plugin));
		commands.put("mute", new MuteCommand(plugin));

		commands.put("banish", new BanishCommand(plugin));
		commands.put("vanish", new VanishCommand(plugin));

		commands.put("tp", new TpCommand(plugin));
		commands.put("summon", new SummonCommand(plugin));
		commands.put("send", new SendCommand(plugin));

		commands.put("notp", new NoTpCommand(plugin));
		commands.put("nosummon", new NoSummonCommand(plugin));
		commands.put("noport", new NoPortCommand(plugin));

		commands.put("home", new HomeCommand(plugin));
		commands.put("sethome", new SetHomeCommand(plugin));
		commands.put("spawn", new SpawnCommand(plugin));
		commands.put("setspawn", new SetSpawnCommand(plugin));
		commands.put("compass", new CompassCommand(plugin));

		commands.put("give", new GiveCommand(plugin));
		commands.put("throw", new ThrowCommand(plugin));
		commands.put("clear", new ClearCommand(plugin));

		commands.put("time", new TimeCommand(plugin));
		commands.put("servertime", new ServerTimeCommand(plugin));

		commands.put("reloadads", new ReloadAdsCommand(plugin));

		commands.put("warp", new WarpCommand(plugin));
		commands.put("setwarp", new SetWarpCommand(plugin));

		commands.put("jail", new JailCommand(plugin));
		commands.put("setjail", new SetJailCommand(plugin));

		commands.put("setportal", new SetPortalCommand(plugin));

		commands.put("isee", new ISeeCommand(plugin));
		commands.put("leash", new LeashCommand(plugin));
		commands.put("bind", new BindCommand(plugin));
		commands.put("god", new GodCommand(plugin));
		commands.put("heal", new HealCommand(plugin));

		commands.put("�", new CheaterCommand(plugin));

		commands.put("setpass", new PasswordCommand(plugin));

		commands.put("rcon", new ConsoleCommand(plugin));

		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_LOGIN, this, Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_KICK, this, Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Priority.High, plugin);
		pm.registerEvent(Event.Type.PLAYER_CHAT, this, Priority.High, plugin);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this, Priority.Normal, plugin);
	}

	@Override
	public void onPlayerLogin(PlayerLoginEvent event) {
		String rank = plugin.playerHelper.GetPlayerRank(event.getPlayer());
		if (rank.equals("banned")) {
			event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "[YB] You're banned");
			return;
		}
		(new HackCheckThread(event.getPlayer().getName())).start();
	}

	class HackCheckThread extends Thread {
		private String plyName;
		public HackCheckThread(String plyNameX) {
			plyName = plyNameX;
		}

		public void run() {
			try {
				URL url = new URL("http://cursecraft.com/mc_validation.php?username=" + plyName);
				URLConnection conn = url.openConnection();
				conn.connect();
				BufferedReader buffre = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String ret = buffre.readLine().trim().toLowerCase();
				buffre.close();
				if (ret.equals("yes")) {
					Player ply = plugin.playerHelper.MatchPlayerSingle(plyName);

					plugin.playerHelper.SendDirectedMessage(ply, "Your account might have been hacked!", '4');
					plugin.playerHelper.SendDirectedMessage(ply, "Check http://bit.ly/eLIUhb for further info!", '4');

					plugin.playerHelper.SendServerMessage("The account of " + ply.getName() + " might have been hacked!", 3, '4');
					plugin.playerHelper.SendServerMessage("Check http://bit.ly/eLIUhb for further info!", 3, '4');
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setDisplayName(plugin.playerHelper.GetPlayerNick(event.getPlayer().getName()));
		
		event.setJoinMessage("�2[+] �e" + plugin.playerHelper.GetFullPlayerName(event.getPlayer()) + "�e joined!");

		plugin.playerHelper.updateToolMappings(event.getPlayer());
	}

	@Override
	public void onPlayerQuit(PlayerEvent event) {
		plugin.getServer().broadcastMessage("�4[-] �e" + plugin.playerHelper.GetFullPlayerName(event.getPlayer()) + "�e disconnected!");
	}

	@Override
	public void onPlayerKick(PlayerKickEvent event) {
		plugin.getServer().broadcastMessage("�4[-] �e" + plugin.playerHelper.GetFullPlayerName(event.getPlayer()) + "�e was kicked (reason: " + event.getReason() + ")!");
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player ply = event.getPlayer();
		if (ply.getHealth() <= 0) {
			event.setCancelled(true);
			return;
		}
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player ply = event.getPlayer();
		Location location = ply.getWorld().getSpawnLocation();
		location.setX(location.getX()+0.5);
		location.setZ(location.getZ()+0.5);
		event.setRespawnLocation(location);
	}

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		String msg = event.getMessage();
		if (msg.charAt(0) == '!') {
			plugin.playerHelper.SendDirectedMessage(event.getPlayer(), "!commands are disabled because they show up in the web chat. Please use /commands.");
			event.setCancelled(true);
			return;
		}
		event.setFormat(plugin.playerHelper.GetPlayerTag(event.getPlayer()) + "%s:�f %s");
	}

	public Hashtable<String,ICommand> commands = new Hashtable<String,ICommand>();
	@Override
	public void onPlayerCommandPreprocess(PlayerChatEvent event) {
		if (runCommand(event.getPlayer(), event.getMessage().substring(1).trim())) {
			event.setCancelled(true);
		}
	}

	public boolean runCommand(Player ply, String baseCmd) {
		int posSpace = baseCmd.indexOf(' ');
		String cmd; String args[]; String argStr;
		if (posSpace < 0) {
			cmd = baseCmd;
			args = new String[0];
			argStr = "";
		}
		else {
			cmd = baseCmd.substring(0, posSpace).trim();
			argStr = baseCmd.substring(posSpace).trim();
			args = argStr.split(" +");
		}
		if (commands.containsKey(cmd)) {
			ICommand icmd = commands.get(cmd);
			try {
				if(!icmd.CanPlayerUseCommand(ply)) {
					throw new PermissionDeniedException();
				}
				Logger.getLogger("Minecraft").log(Level.INFO, "Command: "+ply.getName()+": "+baseCmd);
				icmd.Run(ply,args,argStr);
			}
			catch (YiffBukkitCommandException e) {
				plugin.playerHelper.SendDirectedMessage(ply,e.getMessage(), e.getColor());
			}
			catch (Exception e) {
				if (plugin.playerHelper.GetPlayerLevel(ply) >= 4) {
					plugin.playerHelper.SendDirectedMessage(ply,"Command error: "+e+" in "+e.getStackTrace()[0]);
					e.printStackTrace();
				}
				else {
					plugin.playerHelper.SendDirectedMessage(ply,"Command error!");
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		switch (event.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			try {
				Material itemMaterial = event.getMaterial();
				Player ply = event.getPlayer();
				Integer selflvl = playerHelper.GetPlayerLevel(ply);
				// This will not be logged by bigbrother so I only allowed it for ops+ for now.
				// A fix would be to modify the event a bit to make BB log this. 
				if (selflvl >= 3 && itemMaterial == Material.INK_SACK) {
					Block block = event.getClickedBlock();
					if (block.getType() == Material.WOOL) {
						ItemStack item = event.getItem();
						block.setData((byte)(15 - item.getDurability()));
						int newAmount = item.getAmount()-1;
						if (newAmount > 0)
							item.setAmount(newAmount);
						else
							ply.setItemInHand(null);
					}
				}
			}
			finally {
				Player ply = event.getPlayer();
				Material itemMaterial = event.getMaterial();

				String key = ply.getName()+" "+itemMaterial.name();
				ToolBind runnable = plugin.playerHelper.toolMappings.get(key);
				if (runnable != null) {
					try {
						runnable.run(event);
					}
					catch (YiffBukkitCommandException e) {
						plugin.playerHelper.SendDirectedMessage(ply,e.getMessage(), e.getColor());
					}
					catch (Exception e) {
						if (plugin.playerHelper.GetPlayerLevel(ply) >= 4) {
							plugin.playerHelper.SendDirectedMessage(ply,"Command error: "+e+" in "+e.getStackTrace()[0]);
							e.printStackTrace();
						}
						else {
							plugin.playerHelper.SendDirectedMessage(ply,"Command error!");
						}
					}
				}
			}
			break;
		}
	}

	@Override
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

		Integer selflvl = playerHelper.GetPlayerLevel(ply);
		if(selflvl < 0 || (YiffBukkitBlockListener.blocklevels.containsKey(itemMaterial) && selflvl < YiffBukkitBlockListener.blocklevels.get(itemMaterial))) {
			playerHelper.SendServerMessage(ply.getName() + " tried to spawn illegal block " + itemMaterial.toString());
			item.setType(Material.GOLD_HOE);
			item.setAmount(1);
			item.setDurability(Short.MAX_VALUE);
			return;
		}
	}
}
