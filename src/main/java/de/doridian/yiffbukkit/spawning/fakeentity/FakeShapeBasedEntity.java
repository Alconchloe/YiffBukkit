package de.doridian.yiffbukkit.spawning.fakeentity;

import de.doridian.yiffbukkit.core.YiffBukkit;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.transmute.EntityTypeNotFoundException;
import de.doridian.yiffbukkit.transmute.Shape;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FakeShapeBasedEntity extends FakeEntity {
	private final Shape shape;

	public FakeShapeBasedEntity(Location location, String mobType) throws EntityTypeNotFoundException {
		super(location);

		shape = Shape.getShape(YiffBukkit.instance.transmute, this, mobType);
	}

	public FakeShapeBasedEntity(Location location, int mobType) throws EntityTypeNotFoundException {
		super(location);

		shape = Shape.getShape(YiffBukkit.instance.transmute, this, mobType);
	}

	@Override
	public void send(Player player) {
		shape.createTransmutedEntity(player);
	}

	public void runAction(CommandSender commandSender, String action) throws YiffBukkitCommandException {
		shape.runAction(commandSender, action);
	}

	public Shape getShape() {
		return shape;
	}
}
