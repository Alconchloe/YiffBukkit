package de.doridian.yiffbukkit.advanced.commands;

import com.sk89q.worldedit.blocks.BlockType;
import de.doridian.yiffbukkit.core.util.MessageHelper;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.system.ICommand;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.system.ICommand.Permission;
import net.minecraft.server.v1_7_R1.Item;
import net.minecraft.server.v1_7_R1.MaterialMapColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import static java.lang.Math.*;

@Names("seccam")
@Permission("worldedit.generation.map")
public class SecCamCommand extends ICommand {
	@Override
	public void Run(final Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		final ItemStack itemInHand = ply.getItemInHand();
		final Material itemInHandType = itemInHand.getType();
		if (itemInHandType != Material.MAP)
			throw new YiffBukkitCommandException("Not a map!");

		final MapMeta itemMeta = (MapMeta) itemInHand.getItemMeta();

		final MapView mapView = Bukkit.getMap(itemInHand.getDurability());
		for (MapRenderer mapRenderer : mapView.getRenderers()) {
			mapView.removeRenderer(mapRenderer);
		}

		final double zoom = 1;
		final double viewdist = 9;
		final double ungenau = 1e-13;
		final MapRenderer mapRenderer = new MapRenderer() {
			boolean die = false;
			double t = 0.0;

			@Override
			public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
				if (die)
					return;

				die = true;
				t += 1.0/20.0;
// cam coords
				final Location location = ply.getEyeLocation();
				final Vector origin = location.toVector();
				double ox = -location.getX();
				double oy = -location.getY();
				double oz = location.getZ();
				double rx = -Math.toRadians(location.getPitch());
				double ry = -Math.toRadians(location.getYaw());
				double rz = 0;
				double sx = Math.sin(rx);
				double cx = Math.cos(rx);
				double sy = Math.sin(ry);
				double cy = Math.cos(ry);
				double sz = Math.sin(rz);
				double cz = Math.cos(rz);
				double xp = 0;
				double yp = 72;
				double zp = 14;
				double rad = 9;

				for (int xp2 = 0; xp2 < 128; ++xp2) {
					for (int yp2 = 0; yp2 < 128; ++yp2) {
						double x = xp2 / 64.0 - 1.0;
						double y = yp2 / 64.0 - 1.0;
						final byte color = genPixel(origin, sx, cx, sy, cy, sz, cz, xp, yp, zp, rad, x, y);

						mapCanvas.setPixel(xp2, yp2, color);
					}
				}
			}

			private byte genPixel(Vector origin, double sx, double cx, double sy, double cy, double sz, double cz, double xp, double yp, double zp, double rad, double x, double y) {
				// proj. plane coords
				double dx = x / zoom;
				double dy = y / zoom;
				double dz = 1;

				// z rotation
				{
					final double dx1 = dx * cz - dy * sz;
					dy = dx * sz + dy * cz;
					dx = dx1;
				}

				// x rotation
				{
					final double dy1 = dy * cx - dz * sx;
					dz = dy * sx + dz * cx;
					dy = dy1;
				}

				// y rotation
				{
					final double dx1 = dx * cy - dz * sy;
					final double dz1 = dx * sy + dz * cy;
					dx = dx1; dz = dz1;
				}

				final BlockIterator blockIterator = new BlockIterator(ply.getWorld(), origin, new Vector(-dx, -dy, dz), 0, 300);
				while (blockIterator.hasNext()) {
					final Block next = blockIterator.next();
					if (next.getY() > 255)
						continue;
					if (next.getY() < 0)
						continue;

					Material type = next.getType();
					switch (type) {
					case WATER:
					case STATIONARY_WATER:
					case LAVA:
					case STATIONARY_LAVA:
						break;
					default:
						if (BlockType.canPassThrough(next.getTypeId(), next.getData()))
							continue;
					}

					if (next.getRelative(0, 1, 0).getType() == Material.SNOW)
						type = Material.SNOW;
					final net.minecraft.server.v1_7_R1.Block notchBlock = CraftMagicNumbers.getBlock(type);
					final MaterialMapColor materialMapColor = notchBlock.f(next.getData());
					int offset = 0;
					return (byte) (materialMapColor.M * 4 + offset);
				}
				return 0;
			}
		};
		mapView.addRenderer(mapRenderer);

		MessageHelper.sendMessage(ply, "" + mapView.getId());

		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (false) ply.sendMap(mapView);
				final Player tomyLobo = Bukkit.getPlayerExact("TomyLobo");
				if (tomyLobo != ply && tomyLobo != null) {
					tomyLobo.sendMap(mapView);
				}
			}
		}, 0, 8);
		ply.sendMap(mapView);
	}

	private static double sqr(double v) {
		return v*v;
	}
}
