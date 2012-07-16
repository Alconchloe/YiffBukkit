package de.doridian.yiffbukkit.advanced.listeners;

import de.doridian.yiffbukkitsplit.YiffBukkit;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.server.Packet30Entity;
import net.minecraft.server.Packet34EntityTeleport;
import net.minecraft.server.Packet35EntityHeadRotation;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.server.Packet;
import org.bukkit.event.server.PacketListener;

public class YiffBukkitHeadChopOffListener extends PacketListener implements Listener {
	private final static byte CHOPPED_PITCH = (byte)128;

	final YiffBukkit plugin;
	public YiffBukkitHeadChopOffListener(YiffBukkit plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		//PacketListener.addPacketListener(true, 30, this, plugin);
		//PacketListener.addPacketListener(true, 31, this, plugin);
		PacketListener.addPacketListener(true, 32, this, plugin);
		PacketListener.addPacketListener(true, 33, this, plugin);
		PacketListener.addPacketListener(true, 34, this, plugin);

		PacketListener.addPacketListener(true, 35, this, plugin);
	}

	private TIntHashSet choppedEntities = new TIntHashSet();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		int eid = event.getEntity().getEntityId();
		choppedEntities.remove(eid);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.isCancelled() || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
		if(!(event instanceof EntityDamageByEntityEvent)) return;
		Entity damagedEntity = event.getEntity();
		if(damagedEntity instanceof Player || !(damagedEntity instanceof LivingEntity)) return;
		if(choppedEntities.contains(damagedEntity.getEntityId())) return;
		Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
		if(!(damager instanceof Player)) return;
		Player damagerPly = (Player)damager;
		Material item = damagerPly.getItemInHand().getType();
		if(item != Material.DIAMOND_SPADE && item != Material.GOLD_SPADE && item != Material.IRON_SPADE && item != Material.WOOD_SPADE && item != Material.STONE_SPADE) return;
		choppedEntities.add(damagedEntity.getEntityId());
	}

	private net.minecraft.server.Entity getEntityByID(int eid, World world) {
		return ((CraftWorld)world).getHandle().getEntity(eid);
	}

	@Override
	public boolean onOutgoingPacket(Player ply, int packetID, Packet packetRaw) {
		switch (packetID) {
			case 34:
				Packet34EntityTeleport packet34 = (Packet34EntityTeleport)packetRaw;
				if(choppedEntities.contains(packet34.a)) {
					packet34.f = CHOPPED_PITCH;
				}
				break;
			case 32:
			case 33:
				Packet30Entity packet30 = (Packet30Entity)packetRaw;
				if(choppedEntities.contains(packet30.a)) {
					packet30.f = CHOPPED_PITCH;
				}
				break;
			case 35:
				Packet35EntityHeadRotation packet35 = (Packet35EntityHeadRotation)packetRaw;
				if(choppedEntities.contains(packet35.a)) {
					float yaw = getEntityByID(packet35.a, ply.getWorld()).yaw;
					packet35.b = (byte)(((yaw % 360) / 360) * 255);
				}
				break;
		}
		return true;
	}

	@Override
	public boolean onIncomingPacket(Player ply, int packetID, Packet packet) {
		return true;
	}
}