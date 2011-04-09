package de.doridian.yiffbukkit.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.ToolBind;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.YiffBukkitPlayerListener;
import de.doridian.yiffbukkit.commands.ICommand.*;
import de.doridian.yiffbukkit.util.Utils;


@Names("throw")
@Help(
		"Binds creature/tnt/sand/gravel/minecart/self('me')/target('this') throwing to your current tool. Right-click to use.\n"+
		"Unbind by typing '/throw' without arguments. You can stack mobs by separating them with a plus (+).\n"+
		"Data values:\n"+
		"  sheep:<dye color>|party|camo|sheared\n"+
		"  wolf:angry|tame|sit (can be combined)"
)
@Usage("[<type>[ <forward>[ <up>[ <left>]]]]")
@Level(4)
public class ThrowCommand extends ICommand {
	public ThrowCommand(YiffBukkitPlayerListener playerListener) {
		super(playerListener);
	}

	@Override
	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		Material toolType = ply.getItemInHand().getType();

		if (argStr.isEmpty()) {
			playerHelper.addToolMapping(ply, toolType, null);

			playerHelper.SendDirectedMessage(ply, "Unbound your current tool (�e"+toolType.name()+"�f).");

			return;
		}

		final Vector speed = new Vector(2,0,0);
		try {
			if (args.length >= 2) {
				speed.setX(Double.valueOf(args[1]));
				if (args.length >= 3) {
					speed.setY(Double.valueOf(args[2]));
					if (args.length >= 4) {
						speed.setZ(Double.valueOf(args[3]));
					}
				}
			}
		} catch (NumberFormatException e) {
			throw new YiffBukkitCommandException("Number expected", e);
		}

		String typeName = args[0].toUpperCase();

		ToolBind runnable;
		if (typeName.equals("ME")) {
			runnable = new ToolBind("/throw me", ply) {
				public void run(PlayerInteractEvent event) {
					Player player = event.getPlayer();
					final Location location = player.getLocation();

					final Vector direction = Utils.toWorldAxis(location, speed);
					if (player.isInsideVehicle()) {
						Entity vehicle = ((CraftPlayer)player).getHandle().vehicle.getBukkitEntity();//ply.getVehicle()
						vehicle.setVelocity(direction);
					}
					else {
						player.setVelocity(direction);
					}
				}
			};
		}
		else {
			final String[] types = typeName.split("\\+");

			runnable = new ToolBind("/throw "+typeName, ply) {
				public void run(PlayerInteractEvent event) throws YiffBukkitCommandException {
					Player player = event.getPlayer();
					final Location location = player.getEyeLocation();
					Entity entity = plugin.utils.buildMob(types, player, null, location);
					entity.setVelocity(Utils.toWorldAxis(location, speed));

				}
			};
		}

		playerHelper.addToolMapping(ply, toolType, runnable);

		playerHelper.SendDirectedMessage(ply, "Bound �9"+typeName+"�f to your current tool (�e"+toolType.name()+"�f). Right-click to use.");
	}
}
