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
package de.doridian.yiffbukkit.core.util;

import com.google.common.base.Predicate;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public class LevelPredicate implements Predicate<CommandSender> {
	private final int minLevel;

	public LevelPredicate(final int minLevel) {
		this.minLevel = minLevel;
	}

	@Override
	public boolean apply(@Nullable CommandSender player) {
		if(player == null)
			return false;
		return PlayerHelper.getPlayerLevel(player) >= minLevel;
	}
}
