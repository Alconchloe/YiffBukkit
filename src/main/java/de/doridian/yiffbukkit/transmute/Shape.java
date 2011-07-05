package de.doridian.yiffbukkit.transmute;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkitCommandException;

public abstract class Shape {
	final protected Transmute transmute;
	final protected int entityID;
	final protected Player player;

	protected Shape(Transmute transmute, Player player) {
		this.transmute = transmute;
		this.player = player;
		entityID = player.getEntityId();
	}

	public void deleteEntity() {
		transmute.plugin.playerHelper.sendPacketToPlayersAround(player.getLocation(), 1024, new Packet29DestroyEntity(player.getEntityId()), player);
	}

	public void createOriginalEntity() {
		transmute.plugin.playerHelper.sendPacketToPlayersAround(player.getLocation(), 1024, createPlayerSpawnPacket(), player);
	}

	private Packet20NamedEntitySpawn createPlayerSpawnPacket() {
		return new Packet20NamedEntitySpawn(((CraftPlayer)player).getHandle());
	}

	abstract public void createTransmutedEntity();
	abstract public void createTransmutedEntity(Player forPlayer);
	
	abstract public void runAction(String action) throws YiffBukkitCommandException;
}
