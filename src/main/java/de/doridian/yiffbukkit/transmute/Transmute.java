package de.doridian.yiffbukkit.transmute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.Packet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;

public class Transmute {
	final YiffBukkit plugin;
	private final TransmutePacketListener transmutePacketListener;
	@SuppressWarnings("unused")
	private TransmutePlayerListener transmutePlayerListener;
	private final Map<Integer, Shape> transmuted = new HashMap<Integer, Shape>();

	public Transmute(YiffBukkit plugin) {
		this.plugin = plugin;
		transmutePacketListener = new TransmutePacketListener(this);
		transmutePlayerListener = new TransmutePlayerListener(this);

		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				// clean up ignored packets
				long minTimestamp = System.currentTimeMillis() - 1000;

				for (Iterator<Packet> iterator = transmutePacketListener.ignoredPackets.iterator(); iterator.hasNext(); ) {
					final net.minecraft.server.Packet packet = (net.minecraft.server.Packet) iterator.next();

					if (packet.timestamp < minTimestamp)
						iterator.remove();
				}

				// clean up transmuted entities
				for (Iterator<Entry<Integer, Shape>> iterator = transmuted.entrySet().iterator(); iterator.hasNext(); ) {
					final Entry<Integer, Shape> entry = iterator.next();
					final Shape shape = entry.getValue();

					if (shape.entity.isDead())
						iterator.remove();
				}
			}
		}, 0, 200);
	}

	public boolean isTransmuted(int entityID) {
		return transmuted.containsKey(entityID);
	}

	public boolean isTransmuted(Entity entity) {
		return transmuted.containsKey(entity.getEntityId());
	}

	public boolean isTransmuted(net.minecraft.server.Entity entity) {
		return transmuted.containsKey(entity.id);
	}

	public Shape getShape(int entityID) {
		return transmuted.get(entityID);
	}

	public Shape getShape(Entity entity) {
		return transmuted.get(entity.getEntityId());
	}

	public Shape getShape(net.minecraft.server.Entity entity) {
		return transmuted.get(entity.id);
	}

	public void setShape(Entity entity, Shape shape) {
		if (shape.entity != entity)
			throw new IllegalArgumentException("Assigned a shape to the wrong player!");

		transmuted.put(entity.getEntityId(), shape);
		shape.deleteEntity();
		shape.createTransmutedEntity();
	}

	public void setShape(Player player, Entity entity, int mobType) {
		setShape(entity, Shape.getShape(this, player, entity, mobType));
	}

	public void setShape(Player player, Entity entity, String mobType) {
		setShape(entity, Shape.getShape(this, player, entity, mobType));
	}

	public Shape resetShape(Entity entity) {
		Shape shape = removeShape(entity);
		if (shape != null)
			shape.createOriginalEntity();

		return shape;
	}

	public Shape removeShape(Entity entity) {
		Shape shape = transmuted.remove(entity.getEntityId());
		if (shape != null)
			shape.deleteEntity();

		return shape;
	}

	Packet ignorePacket(Packet packet) {
		transmutePacketListener.ignoredPackets.add(packet);
		return packet;
	}
}
