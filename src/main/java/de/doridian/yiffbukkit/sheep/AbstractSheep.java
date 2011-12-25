package de.doridian.yiffbukkit.sheep;

import de.doridian.yiffbukkit.YiffBukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;

public abstract class AbstractSheep implements Runnable {
	private final YiffBukkit plugin;
	protected final Sheep sheep;
	private final int taskId;

	public AbstractSheep(YiffBukkit plugin, Sheep sheep) {
		this.plugin = plugin;
		this.sheep = sheep;

		taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 10);
	}

	@Override
	public void run() {
		if (sheep.isDead() || sheep.isSheared()) {
			plugin.getServer().getScheduler().cancelTask(taskId);
			return;
		}

		final DyeColor newColor = getColor();
		if (newColor != null && newColor != sheep.getColor()) 
			sheep.setColor(newColor);
	}

	public abstract DyeColor getColor();
}
