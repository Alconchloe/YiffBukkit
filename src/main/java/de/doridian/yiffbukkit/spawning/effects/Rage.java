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
package de.doridian.yiffbukkit.spawning.effects;

import de.doridian.yiffbukkit.core.YiffBukkit;
import de.doridian.yiffbukkit.spawning.SpawnUtils;
import de.doridian.yiffbukkit.spawning.effects.system.EffectProperties;
import de.doridian.yiffbukkit.spawning.effects.system.YBEffect;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_7_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_7_R3.PacketPlayOutEntityLook;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

@EffectProperties(
		name = "rage",
		potionColor = 12,
		potionTrail = true
)
public class Rage extends YBEffect {
	// TODO: area/direct hit with different lengths
	private static final int ticks = 100;

	private int i = 0;
	final Random random = new Random();

	public Rage(Entity entity) {
		super(entity);
	}

	@Override
	public void start() {
		if (!(entity instanceof CraftLivingEntity)) {
			done();
			return;
		}

		scheduleSyncRepeating(0, 1);
	}

	@Override
	public void runEffect() {
		final EntityLiving notchEntity = ((CraftLivingEntity) entity).getHandle();
		Location location = entity.getLocation();

		byte yaw = (byte)(random.nextInt(255)-128);
		byte pitch = (byte)(random.nextInt(255)-128);
		if (entity instanceof Player) {
			// damage animation
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutAnimation(notchEntity, 2));

			// arm animation
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutAnimation(notchEntity, 1));

			// random looking
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutEntityLook(entity.getEntityId(), yaw, pitch), (Player) entity);
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutEntityHeadRotation(notchEntity, yaw), (Player) entity);
		}
		else {
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutEntityLook(entity.getEntityId(), yaw, pitch), null);
			YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(location, 32, new PacketPlayOutEntityHeadRotation(notchEntity, (byte) random.nextInt(256)), null);
		}

		if (++i > ticks) {
			done();
			cancel();
		}
	}

	public static class PotionTrail extends YBEffect.PotionTrail {
		public PotionTrail(Entity entity) {
			super(entity);
		}

		@Override
		protected void renderEffect(Location location) {
			SpawnUtils.makeParticles(location, new Vector(.1, .1, .1), 0, 10, "flame");
		}
	}
}
