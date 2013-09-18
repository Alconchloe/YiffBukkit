package de.doridian.yiffbukkit.spawning.fakeentity;

import de.doridian.yiffbukkitsplit.YiffBukkit;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.ItemStack;
import net.minecraft.server.v1_6_R2.MathHelper;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet28EntityVelocity;
import net.minecraft.server.v1_6_R2.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R2.Packet34EntityTeleport;
import net.minecraft.server.v1_6_R2.Packet38EntityStatus;
import net.minecraft.server.v1_6_R2.Packet40EntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.UUID;

public abstract class FakeEntity extends AbstractEntity {
	static int lastFakeEntityId = 1000000000;

	public final int entityId;
	public Location location;
	private boolean isDead;

	private DataWatcher datawatcher = new DataWatcher();

	@Override
	public boolean isOnGround() {
		return true;
	}

	public FakeEntity(Location location) {
		entityId = ++lastFakeEntityId;
		this.location = location;
	}

	public void send() {
		for (Player player : location.getWorld().getPlayers()) {
			send(player);
		}
	}

	abstract public void send(Player player);

	private void delete() {
		for (Player player : location.getWorld().getPlayers()) {
			delete(player);
		}
	}

	private void delete(Player player) {
		PlayerHelper.sendPacketToPlayer(player, new Packet29DestroyEntity(entityId));
	}

	@Override
	public void setVelocity(Vector velocity) {
		for (Player player : location.getWorld().getPlayers()) {
			PlayerHelper.sendPacketToPlayer(player, new Packet28EntityVelocity(entityId, velocity.getX(), velocity.getY(), velocity.getZ()));
		}
	}

	@Override
	public boolean teleport(Location location) {
		this.location = location;
		for (Player player : location.getWorld().getPlayers()) {
			PlayerHelper.sendPacketToPlayer(player, new Packet34EntityTeleport(entityId, MathHelper.floor(location.getX()*32.0D), MathHelper.floor(location.getY()*32.0D), MathHelper.floor(location.getZ()*32.0D), (byte)0, (byte)0));
		}
		return true;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public World getWorld() {
		return location.getWorld();
	}

	@Override
	public List<Entity> getNearbyEntities(double x, double y, double z) {
		return null;
		/*
		EntityPlayer entity = new EntityPlayer(null, null, null, null);
		@SuppressWarnings("unchecked")
		List<Entity> notchEntityList = ((CraftWorld)world).getHandle().b(entity, entity.boundingBox.b(x, y, z));
		List<org.bukkit.entity.Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(notchEntityList.size());

		for (Entity e: notchEntityList) {
			bukkitEntityList.add(e.getBukkitEntity());
		}
		return bukkitEntityList;
		*/
	}

	@Override
	public int getEntityId() {
		return entityId;
	}

	@Override
	public void remove() {
		delete();
		isDead = true;
	}

	@Override
	public boolean isDead() {
		return isDead;
	}

	public void setData(int index, Object value) {
		sendPacketToPlayersAround(createMetadataPacket(index, value));
	}

	protected Packet40EntityMetadata createMetadataPacket(int index, Object value) {
		if (value instanceof ItemStack) {
			try {
				// create entry
				datawatcher.a(index, 5); // v1_6_R2
			} catch (Exception e) { }

			// put the actual data in
			datawatcher.watch(index, value);

			// mark dirty
			datawatcher.h(index);

			final Packet40EntityMetadata packet40EntityMetadata = new Packet40EntityMetadata(entityId, datawatcher, false);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			packet40EntityMetadata.a(new DataOutputStream(baos));
			return packet40EntityMetadata;
		}
		else {
			try {
				// create entry
				datawatcher.a(index, value.getClass().getConstructor(String.class).newInstance("0")); // v1_6_R2
				// mark dirty
				datawatcher.watch(index, value.getClass().getConstructor(String.class).newInstance("1"));
			}
			catch (Exception e) { }

			// put the actual data in
			datawatcher.watch(index, value);

			return new Packet40EntityMetadata(entityId, datawatcher, false);
		}
	}

	public void sendEntityStatus(byte status) {
		sendPacketToPlayersAround(new Packet38EntityStatus(entityId, status));
	}

	public void sendPacketToPlayersAround(Packet packet) {
		YiffBukkit.instance.playerHelper.sendPacketToPlayersAround(getLocation(), 1024, packet);
	}
}
