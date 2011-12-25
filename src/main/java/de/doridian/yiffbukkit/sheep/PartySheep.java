package de.doridian.yiffbukkit.sheep;

import de.doridian.yiffbukkit.YiffBukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;

public class PartySheep extends AbstractSheep {
	public PartySheep(YiffBukkit plugin, Sheep sheep) {
		super(plugin, sheep);
	}

	@Override
	public DyeColor getColor()  {
		DyeColor[] dyes = DyeColor.values();
		DyeColor dyeColor = dyes[(int)Math.floor(dyes.length*Math.random())];
		return dyeColor;
	}
}
