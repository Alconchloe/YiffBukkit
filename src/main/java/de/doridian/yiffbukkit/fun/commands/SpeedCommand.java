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
package de.doridian.yiffbukkit.fun.commands;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

@ICommand.Names("speed")
@ICommand.Help("Changes speedmode")
@ICommand.Usage("[speed] [player]")
@ICommand.Permission("yiffbukkit.players.speed")
public class SpeedCommand extends ICommand implements Runnable {
	private final HashMap<String, Integer> states = new HashMap<>();

	public SpeedCommand() {
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 300);
	}

	@Override
	public void run() {
		for(Map.Entry<String, Integer> state : states.entrySet()) {
			Player ply = plugin.getServer().getPlayerExact(state.getKey());
			if(ply == null || !ply.isOnline())
				continue;
			addPotionEffect(ply, state.getValue());
		}
	}

	private void addPotionEffect(Player ply, int amp) {
		PotionEffect potionEffect = new PotionEffect(PotionEffectType.SPEED, 1200, amp - 1);
		ply.addPotionEffect(potionEffect, true);
	}

	@Override
	public void run(CommandSender commandSender, String[] args, String argStr, String commandName) throws YiffBukkitCommandException {
		int amplifier = 0;
		if(args.length > 0) {
			amplifier = Integer.parseInt(args[0]);
		}
		Player target = null;
		if(args.length > 1) {
			target = plugin.playerHelper.matchPlayerSingle(args[1]);
		} else {
			target = asPlayer(commandSender);
		}

		if(!target.equals(commandSender)) {
			if(!commandSender.hasPermission("yiffbukkit.player.speed.others")) {
				throw new PermissionDeniedException();
			}
		}

		if(target.isOnline()) {
			if(amplifier == 0) {
				states.remove(target.getName().toLowerCase());
				target.removePotionEffect(PotionEffectType.SPEED);
			} else {
				states.put(target.getName().toLowerCase(), amplifier);
				addPotionEffect(target, amplifier);
			}
		}
	}
}
