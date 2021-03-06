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
package de.doridian.yiffbukkit.warp.commands;

import de.doridian.yiffbukkit.core.util.PlayerHelper;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import de.doridian.yiffbukkit.main.util.Utils;
import org.bukkit.entity.Player;

import java.util.Set;

@Names("sethome")
@Help("Sets your home position (see /home)")
@ICommand.BooleanFlags("ld")
@ICommand.Usage("[-ld] [name]")
@Permission("yiffbukkit.teleport.basic.sethome")
public class SetHomeCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr, String commandName) throws YiffBukkitCommandException {
		args = parseFlags(args);

		String homeName = "default";
		if(args.length > 0) {
			homeName = args[0];
		}

		if(booleanFlags.contains('l')) {
			Set<String> homeNames = playerHelper.getPlayerHomePositionNames(ply);
			PlayerHelper.sendDirectedMessage(ply, "Home locations [" + ((homeNames.size() > 0) ? (homeNames.size() - 1) : 0) + "/" + playerHelper.getPlayerHomePositionLimit(ply.getUniqueId()) + "]: " + Utils.concat(homeNames, 0, ""));
			return;
		} else if(booleanFlags.contains('d')) {
			playerHelper.setPlayerHomePosition(ply, homeName, null);
			PlayerHelper.sendDirectedMessage(ply, "Home [" + homeName + "] deleted!");
			return;
		}

		playerHelper.setPlayerHomePosition(ply, homeName, ply.getLocation());
		PlayerHelper.sendDirectedMessage(ply, "Home [" + homeName + "] saved!");
	}
}