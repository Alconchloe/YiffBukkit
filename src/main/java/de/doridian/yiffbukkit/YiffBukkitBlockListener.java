package de.doridian.yiffbukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import de.doridian.yiffbukkit.util.PlayerHelper;

/**
 * Handle events for all Block related events
 * @author Doridian
 */
public class YiffBukkitBlockListener extends BlockListener {
	private final YiffBukkit plugin;
	public static final Map<Material,Integer> blocklevels = new HashMap<Material,Integer>();
	public static final Set<Material> flammableBlocks = new HashSet<Material>();
	public static final BlockFace[] flameSpreadDirections = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

	static {
		blocklevels.put(Material.TNT, 4);
		blocklevels.put(Material.BEDROCK, 4);

		blocklevels.put(Material.OBSIDIAN, 1);

		blocklevels.put(Material.WATER, 1);
		blocklevels.put(Material.WATER_BUCKET, 1);
		blocklevels.put(Material.LAVA, 3);
		blocklevels.put(Material.LAVA_BUCKET, 3);

		blocklevels.put(Material.FLINT_AND_STEEL, 3);
		blocklevels.put(Material.FIRE, 3);

		flammableBlocks.add(Material.LOG);
		flammableBlocks.add(Material.WOOD);
		flammableBlocks.add(Material.WOOL);
		flammableBlocks.add(Material.BOOKSHELF);
		flammableBlocks.add(Material.LEAVES);
	}
	private PlayerHelper playerHelper;

	public YiffBukkitBlockListener(YiffBukkit instance) {
		plugin = instance;
		playerHelper = plugin.playerHelper;

		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_CANBUILD, this, Priority.Normal, plugin);
		//pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Priority.Highest, plugin);
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		Player ply = event.getPlayer();
		if (playerHelper.isPlayerDisabled(ply)) {
			event.setBuild(false);
			return;
		}

		final Block block = event.getBlock();
		Material material = block.getType();
		Integer selflvl = playerHelper.GetPlayerLevel(ply);
		if (selflvl < 0 || (blocklevels.containsKey(material) && selflvl < blocklevels.get(material))) {
			playerHelper.SendServerMessage(ply.getName() + " tried to spawn illegal block " + material.toString()+".");
			event.setBuild(false);
		}
		
		
		if (selflvl < 3 && flammableBlocks.contains(material)) {
			for (BlockFace face : flameSpreadDirections) {
				Material neighborMaterial = block.getRelative(face).getType();
				if (neighborMaterial == Material.FIRE) {
					playerHelper.SendServerMessage(ply.getName() + " tried to spawn flammable block " + material.toString() + " near fire.");
					event.setBuild(false);
				}
			}
		}
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		Player ply = event.getPlayer();
		if(playerHelper.isPlayerDisabled(ply)) {
			event.setCancelled(true);
			return;
		}

		if(playerHelper.GetPlayerLevel(ply) < 0 && event.getInstaBreak()) {
			playerHelper.SendServerMessage(ply.getName() + " tried to illegaly break a block!");
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.getChangedType() == Material.PORTAL)
			event.setCancelled(true);
	}
	
	@Override
	public void onBlockCanBuild(BlockCanBuildEvent event)
	{
		if (event.isBuildable() == false) {
			if (event.getMaterial() == Material.FENCE) {
				event.setBuildable(true);
			}
		}
	}
}
