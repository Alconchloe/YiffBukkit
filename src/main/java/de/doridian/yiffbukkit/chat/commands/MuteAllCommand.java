package de.doridian.yiffbukkit.chat.commands;

import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@Names("muteall")
@Help("Mutes all player chat")
@Usage("[on|off]")
@Permission("yiffbukkit.users.muteall")
public class MuteAllCommand extends ICommand implements Listener {
	private boolean muteall = false;

	public MuteAllCommand() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(PlayerChatEvent event) {
		if (muteall && !event.getPlayer().hasPermission("yiffbukkit.users.muteall")) {
			PlayerHelper.sendDirectedMessage(event.getPlayer(), "Server chat is disabled at this time for all users.");
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (muteall && !event.getPlayer().hasPermission("yiffbukkit.users.muteall")) {
			String fullCmd = event.getMessage();
			String cmd = fullCmd.substring(0, fullCmd.indexOf(' ')).trim();
			if(cmd.equals("msg") || cmd.equals("pm") || cmd.equals("conv") || cmd.equals("conversation") || cmd.equals("kick") || cmd.equals("irckick") || cmd.equals("settag") || cmd.equals("setnick") || cmd.equals("setrank") || cmd.equals("jail"))
			{
				PlayerHelper.sendDirectedMessage(event.getPlayer(), "Some server commands have been disabled for all users.");
				event.setCancelled(true);
				return;
			}
		}
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		final String playerName = ply.getName();
		if (muteall) {
			if (argStr.equals("on"))
				throw new YiffBukkitCommandException("The server is already muted!");

			muteall = false;
			playerHelper.sendServerMessage(playerName + " enabled server chat and commands.");
		}
		else {
			if (argStr.equals("off"))
				throw new YiffBukkitCommandException("The server is already unmuted!");

			muteall = true;
			playerHelper.sendServerMessage(playerName + " disabled server chat and commands.");
		}

	}
}