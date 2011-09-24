package de.doridian.yiffbukkit.fakeentity;

import java.util.List;
import java.util.UUID;

import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet28EntityVelocity;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet34EntityTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.util.PlayerHelper;

public abstract class FakeEntity implements Entity {
	static int lastFakeEntityId = 1000000000;

	public final int entityId;
	public Location location;
	private boolean isDead;

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
	public Vector getVelocity() {
		return new Vector();
	}

	@Override
	public World getWorld() {
		return location.getWorld();
	}

	@Override
	public boolean teleport(Entity destination) {
		return teleport(destination.getLocation());
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
	public int getFireTicks() {
		return 0;
	}

	@Override
	public int getMaxFireTicks() {
		return 0;
	}

	@Override
	public void setFireTicks(int ticks) {
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

	@Override
	public Server getServer() {
		return Bukkit.getServer();
	}

	@Override
	public Entity getPassenger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setPassenger(Entity passenger) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getPassenger() == null;
	}

	@Override
	public boolean eject() {
		return setPassenger(null);
	}

	@Override
	public float getFallDistance() {
		return 0;
	}

	@Override
	public void setFallDistance(float distance) {
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent event) {
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return null;
	}

	@Override
	public UUID getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}
}
