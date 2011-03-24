package de.doridian.yiffbukkit.commands;

import net.minecraft.server.EntityFallingSand;
import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFallingSand;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.YiffBukkitCommandException;

public class ThrowCommand extends ICommand {
	public ThrowCommand(YiffBukkit plug) {
		super(plug);
	}

	@Override
	public int GetMinLevel() {
		return 4;
	}

	@Override
	public void Run(final Player ply, String[] args, String argStr) throws YiffBukkitCommandException {
		Material toolType = ply.getItemInHand().getType();

		if (argStr.isEmpty()) {
			playerHelper.addToolMapping(ply, toolType, null);

			playerHelper.SendDirectedMessage(ply, "Unbound your current tool ("+toolType.name()+").");

			return;
		}


		double speed = 2;
		final double finalSpeed = speed;


		Runnable runnable;
		if (args.length >= 2) {
			try {
				speed = Double.valueOf(args[1]);
			} catch (NumberFormatException e) {
				throw new YiffBukkitCommandException("Number expected", e);
			}
		}

		String typeName = args[0].toUpperCase();

		CreatureType type;
		if (typeName.equals("TNT")) {
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					WorldServer notchWorld = ((CraftWorld)ply.getWorld()).getHandle();
					EntityTNTPrimed notchEntity = new EntityTNTPrimed(notchWorld, location.getX(), location.getY(), location.getZ());
					notchWorld.a(notchEntity);

					CraftTNTPrimed tnt = new CraftTNTPrimed((CraftServer)plugin.getServer(), notchEntity);

					tnt.setVelocity(location.getDirection().multiply(finalSpeed));
				}
			};
		}
		else if(typeName.equals("SAND") || typeName.equals("GRAVEL")) {
			final int finalMaterial = Material.valueOf(typeName).getId();
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					WorldServer notchWorld = ((CraftWorld)ply.getWorld()).getHandle();
					EntityFallingSand notchEntity = new EntityFallingSand(notchWorld, location.getX(), location.getY(), location.getZ(), finalMaterial);
					//EntityTNTPrimed notchEntity = new EntityTNTPrimed(notchWorld, location.getX(), location.getY(), location.getZ());
					notchWorld.a(notchEntity);

					CraftFallingSand tnt = new CraftFallingSand((CraftServer)plugin.getServer(), notchEntity);

					tnt.setVelocity(location.getDirection().multiply(finalSpeed));
				}
			};
		}
		else if (typeName.equals("MINECART") || typeName.equals("CART")) {
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					Minecart minecart = ply.getWorld().spawnMinecart(location);

					minecart.setPassenger(ply);
					minecart.setVelocity(location.getDirection().multiply(finalSpeed));
				}
			};
		}
		else if (typeName.equals("BOAT")) {
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					Boat minecart = ply.getWorld().spawnBoat(location);

					minecart.setPassenger(ply);
					minecart.setVelocity(location.getDirection().multiply(finalSpeed));
				}
			};
		}
		else if (typeName.equals("ME")) {
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					
					if (ply.isInsideVehicle()) {
						ply.getVehicle().setVelocity(location.getDirection().multiply(finalSpeed));
					}
					else {
						ply.setVelocity(location.getDirection().multiply(finalSpeed));
					}
				}
			};
		}
		else {
			try {
				type = CreatureType.valueOf(typeName);
			}
			catch (IllegalArgumentException e) {
				throw new YiffBukkitCommandException("Creature type not found", e);
			}

			typeName = type.getName();

			final CreatureType finalType = type;
			runnable = new Runnable() {
				public void run() {
					Location location = ply.getEyeLocation();
					Creature creature = ply.getWorld().spawnCreature(location, finalType);
					if (creature == null)
						playerHelper.SendDirectedMessage(ply, "Failed to spawn creature");

					creature.setVelocity(location.getDirection().multiply(finalSpeed));
				}
			};
		}

		playerHelper.addToolMapping(ply, toolType, runnable);

		playerHelper.SendDirectedMessage(ply, "Bound "+typeName+" to your current tool ("+toolType.name()+"). Right-click to use.");
	}

	@Override
	public String GetHelp() {
		return "Binds creature/tnt/sand/gravel/minecart throwing to your current tool. Right-click to use. Unbind by typing '/throw' without arguments. Throw yourself with '/throw me'";
	}

	@Override
	public String GetUsage() {
		return "[<type> [<speed>]]";
	}
}
