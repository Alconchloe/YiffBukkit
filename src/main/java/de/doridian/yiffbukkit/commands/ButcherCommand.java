package de.doridian.yiffbukkit.commands;

import net.minecraft.server.EntityPlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.util.Vector;

import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.commands.ICommand.*;

@Names("butcher")
@Help(
		"Kills living entities around yourself or a specified target.\n"+
		"The default radius is 20. To kill everything, use a radius\n"+
		"of -1. Players and tamed wolves are never butchered.\n" +
		"Flags:\n"+
		"  -n butcher NPCs too"
)
@Usage("[<target>] [<radius>]")
@Level(3)
@BooleanFlags("n")
@Permission("yiffbukkit.butcher")
public class ButcherCommand extends ICommand {
	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		args = parseFlags(args);
		int radius;
		Player target;
		switch (args.length) {
		case 0:
			//butcher - butcher around yourself in a radius of 20
			radius = 20;
			target = asPlayer(commandSender);

			break;

		case 1:
			try {
				//butcher <radius> - butcher around yourself in the given radius
				radius = args[0].equalsIgnoreCase("all") ? -1 : Integer.parseInt(args[0]);
				target = asPlayer(commandSender);
			}
			catch (NumberFormatException e) {
				//butcher <name> -  butcher around someone else in a radius of 20
				radius = 20;
				target = playerHelper.matchPlayerSingle(args[0]);
			}
			break;

		default:
			try {
				//butcher <radius> <name> - butcher around someone in the given radius
				radius = Integer.parseInt(args[0]);
				target = playerHelper.matchPlayerSingle(args[1]);
			}
			catch (NumberFormatException e) {
				//butcher <name> <...> - not sure yet
				target = playerHelper.matchPlayerSingle(args[0]);

				try {
					//butcher <name> <radius> - butcher around someone in the given radius
					radius = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException e2) {
					throw new YiffBukkitCommandException("Syntax error", e2);
				}
			}
			break;
		}

		int removed = 0;

		final World world;
		if (commandSender instanceof Player)
			world = ((Player)commandSender).getWorld();
		else
			world = plugin.getOrCreateWorld("world", Environment.NORMAL);

		final boolean spareNPCs = !booleanFlags.contains('n');
		if (radius < 0) {
			for (LivingEntity livingEntity : world.getLivingEntities()) {
				if (isSpared(livingEntity, spareNPCs))
					continue;

				livingEntity.remove();
				++removed;
			}

			playerHelper.sendServerMessage(commandSender.getName() + " killed all mobs.", commandSender);
			playerHelper.sendDirectedMessage(commandSender, "Killed "+removed+" mobs.");
			return;
		}

		final Vector targetPos = target.getLocation().toVector();
		final double radiusSquared = radius*radius;
		for (LivingEntity livingEntity : world.getLivingEntities()) {
			if (isSpared(livingEntity, spareNPCs))
				continue;

			final Vector currentPos = livingEntity.getLocation().toVector();
			final double distanceSquared = currentPos.distanceSquared(targetPos);

			if (distanceSquared > radiusSquared)
				continue;

			livingEntity.remove();
			++removed;
		}

		if (target == commandSender) {
			playerHelper.sendServerMessage(commandSender.getName() + " killed all mobs in a radius of "+radius+" around themselves.", commandSender);
			playerHelper.sendDirectedMessage(commandSender, "Killed "+removed+" mobs in a radius of "+radius+" around yourself.");
		}
		else {
			playerHelper.sendServerMessage(commandSender.getName() + " killed all mobs in a radius of "+radius+" around "+target.getName()+".");
			playerHelper.sendDirectedMessage(commandSender, "Killed "+removed+" mobs in a radius of "+radius+" around "+target.getName()+".");
		}
	}

	private boolean isSpared(LivingEntity livingEntity, boolean spareNPCs) {
		if (livingEntity instanceof Player) {
			if (spareNPCs)
				return true;

			final EntityPlayer eply = ((CraftPlayer)livingEntity).getHandle();
			if (eply.world.players.contains(eply))
				return true;

			return false;
		}

		if (livingEntity instanceof Wolf) {
			Wolf wolf = (Wolf) livingEntity;
			if (wolf.isAngry())
				return false;

			if (wolf.isTamed())
				return true;
		}

		return false;
	}
}
