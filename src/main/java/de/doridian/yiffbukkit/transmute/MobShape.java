package de.doridian.yiffbukkit.transmute;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.util.Utils;

public class MobShape extends Shape {
	int mobType;

	protected MobShape(Transmute transmute, Player player, String mobType) {
		this(transmute, player, typeNameToClass(mobType));
	}

	protected MobShape(Transmute transmute, Player player, Class<? extends net.minecraft.server.Entity> mobType) {
		this(transmute, player, classToId(mobType));
	}

	public MobShape(Transmute transmute, Player player, int mobType) {
		super(transmute, player);

		this.mobType = mobType;
	}

	@Override
	public void createTransmutedEntity() {
		transmute.plugin.playerHelper.sendPacketToPlayersAround(player.getLocation(), 1024, createMobSpawnPacket(), player);
	}


	@Override
	public void createTransmutedEntity(Player forPlayer) {
		transmute.plugin.playerHelper.sendPacketToPlayer(forPlayer, createMobSpawnPacket());
	}

	@Override
	public void createOriginalEntity() {
		transmute.plugin.playerHelper.sendPacketToPlayersAround(player.getLocation(), 1024, createPlayerSpawnPacket(), player);
	}

	private Packet24MobSpawn createMobSpawnPacket() {
		Location location = player.getLocation();

		final Packet24MobSpawn p24 = new Packet24MobSpawn();

		p24.a = entityID;
		p24.b = (byte) mobType;
		p24.c = MathHelper.floor(location.getX() * 32.0D);
		p24.d = MathHelper.floor(location.getY() * 32.0D);
		p24.e = MathHelper.floor(location.getZ() * 32.0D);
		p24.f = (byte) ((int) (location.getYaw() * 256.0F / 360.0F));
		p24.g = (byte) ((int) (location.getPitch() * 256.0F / 360.0F));
		EntityLiving entityliving = ((CraftPlayer)player).getHandle();
		DataWatcher dataWatcher = entityliving.Z();
		Utils.setPrivateValue(Packet24MobSpawn.class, p24, "h", dataWatcher);
		return p24;
	}

	private Packet20NamedEntitySpawn createPlayerSpawnPacket() {
		return new Packet20NamedEntitySpawn(((CraftPlayer)player).getHandle());
	}


	private static final Class<? extends net.minecraft.server.Entity> typeNameToClass(String mobType) {
		Map<String, Class<? extends net.minecraft.server.Entity>> typeNameToClass = Utils.getPrivateValue(EntityTypes.class, null, "a");

		for (Entry<String, Class<? extends Entity>> entry : typeNameToClass.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(mobType))
				return entry.getValue();
		}
		return null;
		//return typeNameToClass.get(mobType);
	}
	private static final int classToId(Class<? extends Entity> mobType) {
		Map<Class<? extends net.minecraft.server.Entity>, Integer> classToId = Utils.getPrivateValue(EntityTypes.class, null, "d");

		return classToId.get(mobType);
	}
}
