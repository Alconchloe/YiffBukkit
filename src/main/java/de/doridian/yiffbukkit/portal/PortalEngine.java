package de.doridian.yiffbukkit.portal;

import de.doridian.yiffbukkit.main.util.Utils;
import de.doridian.yiffbukkit.portal.listeners.PortalPlayerListener;
import de.doridian.yiffbukkitsplit.YiffBukkit;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class PortalEngine {
	@SuppressWarnings("unused")
	private YiffBukkit plugin;
	public Map<String, PortalPair> portals = new HashMap<String, PortalPair>();

	public PortalEngine(YiffBukkit plugin) {
		this.plugin = plugin;
		new PortalPlayerListener();
	}

	class Portal {
		private Vector anchor;
		private Vector normal;
		private Vector zAxis; // TODO: find a better name

		public Portal(Vector anchor, Vector normal) {
			this.anchor = anchor;
			this.normal = normal;
			this.zAxis = new Vector(0, 1, 0).crossProduct(normal);
		}

		public Portal(Block block, BlockFace blockFace) {
			this(
					block.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5)),
					new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ())
			);
		}

		public Vector toLocal(Vector worldPos) {
			Vector offset = worldPos.clone().subtract(anchor);
			return toLocalOffset(offset);
		}

		public Vector toLocalOffset(Vector offset) {
			double x = offset.dot(normal);
			double y = offset.getY(); // simplification!
			double z = offset.dot(zAxis);
			return new Vector(x, y, z);
		}

		public Vector toWorld(Vector localPos) {
			Vector ret = toWorldOffset(localPos);
			ret.add(anchor);

			return ret;
		}

		public Vector toWorldOffset(Vector localPos) {
			Vector ret = new Vector(0, localPos.getY(), 0);

			ret.add(normal.clone().multiply(localPos.getX()));
			ret.add(zAxis.clone().multiply(localPos.getZ()));
			return ret;
		}

		public boolean isBehind(Vector pos) {
			double dot = pos.clone().subtract(anchor).dot(normal);
			return dot < 0;
		}
	}

	public class PortalPair {
		private final Portal in;
		private final Portal out;
		private final float yawOffset;

		public PortalPair(Portal in, Portal out) {
			this.in = in;
			this.out = out;
			this.yawOffset = (float) Utils.vectorToYaw(getOffsetOnOtherSide(new Vector(0, 0, 1)));
		}

		public Vector getPosOnOtherSide(Vector pos) {
			return out.toWorld(in.toLocal(pos));
		}

		public Vector getOffsetOnOtherSide(Vector offset) {
			return out.toWorldOffset(in.toLocalOffset(offset));
		}

		public void moveThroughPortal(Entity entity) {
			Location location = entity.getLocation();
			Vector velocity = entity.getVelocity();

			location = getPosOnOtherSide(location.toVector()).toLocation(location.getWorld(), location.getYaw()+yawOffset, location.getPitch());
			velocity = getOffsetOnOtherSide(velocity);

			entity.teleport(location);
			entity.setVelocity(velocity);
		}
	}

	public void addPortal(String name, Block blockIn, BlockFace blockFaceIn, Block blockOut, BlockFace blockFaceOut) {
		portals.put(name, new PortalPair(new Portal(blockIn, blockFaceIn), new Portal(blockOut, blockFaceOut)));
	}

	public void handlePortal(PlayerMoveEvent event) {
		if (portals.isEmpty())
			return;

		final PortalPair portalPair = portals.values().iterator().next();
		final Portal portal = portalPair.in;
		
		//plugin.sendConsoleMsg("move in portal");

		Vector from = event.getFrom().toVector();
		if (portal.anchor.distanceSquared(from) > 25)
			return;

		if (portal.isBehind(from))
			return;

		Vector to = event.getTo().toVector();
		if (!portal.isBehind(to))
			return;

		portalPair.moveThroughPortal(event.getPlayer());
		PlayerHelper.sendDirectedMessage(event.getPlayer(), "woosh");
		//plugin.sendConsoleMsg("woosh");
	}
}
