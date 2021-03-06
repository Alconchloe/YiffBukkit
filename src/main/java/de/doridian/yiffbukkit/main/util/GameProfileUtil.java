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
package de.doridian.yiffbukkit.main.util;

import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;

import java.util.Iterator;
import java.util.UUID;

public class GameProfileUtil {
	public static GameProfile getFilledGameProfile(UUID uuid, String name) {
		GameProfile gameprofile = MinecraftServer.getServer().getUserCache().a(uuid);

		if (gameprofile == null)
			gameprofile = new GameProfile(uuid, name);

		gameprofile = MinecraftServer.getServer().av().fillProfileProperties(gameprofile, true);

		Iterator iterator = gameprofile.getProperties().values().iterator();

		return gameprofile;
	}
}
