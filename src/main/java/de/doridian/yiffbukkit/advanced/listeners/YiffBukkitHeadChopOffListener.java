package de.doridian.yiffbukkit.advanced.listeners;

import de.doridian.yiffbukkit.advanced.packetlistener.YBPacketListener;
import de.doridian.yiffbukkit.componentsystem.YBListener;
import de.doridian.yiffbukkitsplit.YiffBukkit;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet30Entity;
import net.minecraft.server.v1_6_R2.Packet34EntityTeleport;
import net.minecraft.server.v1_6_R2.Packet35EntityHeadRotation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class YiffBukkitHeadChopOffListener extends YBPacketListener implements Listener, YBListener {
	private final static byte CHOPPED_PITCH = (byte)128;

	public YiffBukkitHeadChopOffListener(YiffBukkit plugin) {
		this();
	}

	public YiffBukkitHeadChopOffListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, YiffBukkit.instance);

		register(PacketDirection.OUTGOING, 32);
		register(PacketDirection.OUTGOING, 33);
		register(PacketDirection.OUTGOING, 34);
		register(PacketDirection.OUTGOING, 35);
	}

	private TIntHashSet choppedEntities = new TIntHashSet();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		int eid = event.getEntity().getEntityId();
		choppedEntities.remove(eid);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
			return;

		if (!(event instanceof EntityDamageByEntityEvent))
			return;

		final Entity damagedEntity = event.getEntity();
		if (damagedEntity instanceof Player)
			return;

		if (!(damagedEntity instanceof LivingEntity))
			return;

		final int entityId = damagedEntity.getEntityId();
		if (choppedEntities.contains(entityId))
			return;

		final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
		if (!(damager instanceof Player))
			return;

		final Player damagerPly = (Player) damager;
		final Material item = damagerPly.getItemInHand().getType();
		if (item != Material.DIAMOND_SPADE &&
		    item != Material.GOLD_SPADE &&
		    item != Material.IRON_SPADE &&
		    item != Material.WOOD_SPADE &&
		    item != Material.STONE_SPADE
		)
			return;

		choppedEntities.add(entityId);
	}

	private net.minecraft.server.v1_6_R2.Entity getEntityByID(int eid, World world) {
		return ((CraftWorld)world).getHandle().getEntity(eid);
	}

	@Override
	public boolean onOutgoingPacket(Player ply, int packetID, Packet packetRaw) {
		switch (packetID) {
		case 34:
			final Packet34EntityTeleport packet34 = (Packet34EntityTeleport) packetRaw;
			if (!choppedEntities.contains(packet34.a))
				break;

			packet34.f = CHOPPED_PITCH;

			break;

		case 32:
		case 33:
			final Packet30Entity packet30 = (Packet30Entity) packetRaw;
			if(!choppedEntities.contains(packet30.a))
				break;

			packet30.f = CHOPPED_PITCH;

			break;

		case 35:
			final Packet35EntityHeadRotation packet35 = (Packet35EntityHeadRotation) packetRaw;
			if (!choppedEntities.contains(packet35.a))
				break;

			final float yaw = getEntityByID(packet35.a, ply.getWorld()).yaw;
			packet35.b = (byte)(((yaw % 360) / 360) * 255);

			break;
		}
		return true;
	}
}