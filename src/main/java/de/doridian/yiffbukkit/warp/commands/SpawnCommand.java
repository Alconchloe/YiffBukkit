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
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Names("spawn")
@Help("Teleports you to the spawn position")
@Permission("yiffbukkit.teleport.basic.spawn")
public class SpawnCommand extends ICommand {
	@Override
	public void Run(Player ply, String[] args, String argStr, String commandName) {
		if (playerHelper.isPlayerJailed(ply)) {
			PlayerHelper.sendDirectedMessage(ply, "You are jailed!");
			return;
		}

		Location location = playerHelper.getPlayerSpawnPosition(ply);
		plugin.playerHelper.teleportWithHistory(ply, location);
		PlayerHelper.sendDirectedMessage(ply, "Returned to the spawn!");
	}
}