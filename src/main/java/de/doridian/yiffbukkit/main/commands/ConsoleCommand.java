/**
 * This file is part of YiffBukkit.
 *
 * YiffBukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkit.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.main.commands;

import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.AbusePotential;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Usage;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;

import java.util.logging.Logger;

@Names("rcon")
@Help("Pushes a command to console")
@Usage("<command>")
@Permission("yiffbukkit.rcon")
@AbusePotential
public class ConsoleCommand extends ICommand {
	private static final MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getHandle().getServer();

	private static void sendServerCmd(String cmd, CommandSender sender) {
		if (mcServer != null && !mcServer.isStopped() && mcServer.isRunning()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
		}
		else {
			if (sender != null) {
				sender.sendMessage(ChatColor.RED + "Can't send console command!");
			}
			else {
				Logger.getLogger("Minecraft").log(java.util.logging.Level.WARNING, "Can't send console command!");
			}
		}
	}

	@Override
	public void run(CommandSender commandSender, String[] args, String argStr, String commandName) throws YiffBukkitCommandException {
		sendServerCmd(argStr, commandSender);
	}
}
