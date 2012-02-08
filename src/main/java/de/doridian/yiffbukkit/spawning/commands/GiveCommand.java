package de.doridian.yiffbukkit.spawning.commands;

import de.doridian.yiffbukkit.main.PermissionDeniedException;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkit.main.commands.ICommand;
import de.doridian.yiffbukkit.main.commands.ICommand.Help;
import de.doridian.yiffbukkit.main.commands.ICommand.Names;
import de.doridian.yiffbukkit.main.commands.ICommand.Permission;
import de.doridian.yiffbukkit.main.commands.ICommand.Usage;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

@Names({"give", "item", "i"})
@Help("Gives resource (use _ for spaces in name!)")
@Usage("<name or id> [amount] [player]")
@Permission("yiffbukkitsplit.players.give")
public class GiveCommand extends ICommand {
	private static final Map<String,Material> aliases = new HashMap<String,Material>();
	private static final Map<String,Short> dataValues = new HashMap<String, Short>();
	static {
		aliases.put("wood_shovel", Material.WOOD_SPADE);
		aliases.put("wooden_spade", Material.WOOD_SPADE);
		aliases.put("wooden_shovel", Material.WOOD_SPADE);
		aliases.put("gold_shovel", Material.GOLD_SPADE);
		aliases.put("golden_spade", Material.GOLD_SPADE);
		aliases.put("golden_shovel", Material.GOLD_SPADE);
		aliases.put("golden_pickaxe", Material.GOLD_PICKAXE);
		aliases.put("golden_sword", Material.GOLD_SWORD);
		aliases.put("golden_hoe", Material.GOLD_HOE);
		aliases.put("golden_axe", Material.GOLD_AXE);
		aliases.put("golden_helmet", Material.GOLD_HELMET);
		aliases.put("golden_chestplate", Material.GOLD_CHESTPLATE);
		aliases.put("golden_leggings", Material.GOLD_LEGGINGS);
		aliases.put("golden_boots", Material.GOLD_BOOTS);
		aliases.put("stone_shovel", Material.STONE_SPADE);
		aliases.put("iron_shovel", Material.IRON_SPADE);
		aliases.put("diamond_shovel", Material.DIAMOND_SPADE);

		aliases.put("leaf", Material.LEAVES);
		aliases.put("noteblock", Material.NOTE_BLOCK);
		aliases.put("cloth", Material.WOOL);
		aliases.put("slab", Material.STEP);
		aliases.put("stone_slab", Material.STEP);
		aliases.put("stoneslab", Material.STEP);
		aliases.put("shelf", Material.BOOKSHELF);
		aliases.put("mossy_cobble", Material.MOSSY_COBBLESTONE);
		aliases.put("mobspawner", Material.MOB_SPAWNER);
		aliases.put("wooden_stairs", Material.WOOD_STAIRS);
		aliases.put("cobble_stairs", Material.COBBLESTONE_STAIRS);
		aliases.put("redstone_torch", Material.REDSTONE_TORCH_ON);
		aliases.put("diode_block", Material.DIODE_BLOCK_OFF);
		aliases.put("gunpowder", Material.SULPHUR);
		aliases.put("fish", Material.RAW_FISH);
		aliases.put("button", Material.STONE_BUTTON);
		aliases.put("bukkit", Material.BUCKET);
		aliases.put("water_bukkit", Material.WATER_BUCKET);
		aliases.put("lava_bukkit", Material.LAVA_BUCKET);
		aliases.put("milk_bukkit", Material.MILK_BUCKET);
		aliases.put("yiff_bukkit", Material.MILK_BUCKET);
		aliases.put("yiffbukkitsplit", Material.MILK_BUCKET);
		aliases.put("dye", Material.INK_SACK);
		aliases.put("ink", Material.INK_SACK);
		aliases.put("repeater", Material.DIODE);
		aliases.put("piston", Material.PISTON_BASE);
		aliases.put("sticky_piston", Material.PISTON_STICKY_BASE);
		aliases.put("piston_sticky", Material.PISTON_STICKY_BASE);
		aliases.put("reed", Material.SUGAR_CANE);

		dataValues.put("43:SANDSTONE", (short) 1);
		dataValues.put("43:WOOD", (short) 2);
		dataValues.put("43:COBBLE", (short) 3);
		dataValues.put("43:COBBLESTONE", (short) 3);
		dataValues.put("43:BRICK", (short) 4);
		dataValues.put("43:STONEBRICK", (short) 5);

		dataValues.put("44:SANDSTONE", (short) 1);
		dataValues.put("44:WOOD", (short) 2);
		dataValues.put("44:COBBLE", (short) 3);
		dataValues.put("44:COBBLESTONE", (short) 3);
		dataValues.put("44:BRICK", (short) 4);
		dataValues.put("44:STONEBRICK", (short) 5);

		for (short i = 1; i <= 5; ++i) {
			dataValues.put("43:"+i, i);
			dataValues.put("44:"+i, i);
		}

		dataValues.put("17:REDWOOD", (short) 1);
		dataValues.put("17:DARK", (short) 1);
		dataValues.put("17:PINE", (short) 1);
		dataValues.put("17:SPRUCE", (short) 1);
		dataValues.put("17:BIRCH", (short) 2);
		dataValues.put("17:LIGHT", (short) 2);
	};

	public static Material matchMaterial(String materialName) {
		Material material = aliases.get(materialName.toLowerCase());
		if (material != null)
			return material;

		return Material.matchMaterial(materialName);
	}

	public static short getDataValue(final Material material, String dataName) {
		return dataValues.get(material.getId()+":"+dataName);
	}

	@Override
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {
		Integer count = 1;
		String otherName = null;
		try {
			count = Integer.valueOf(args[1]);
			if (args.length >= 3)
				otherName = args[2];
		}
		catch(Exception e) {
			if (args.length >= 2)
				otherName = args[1];
		}

		final Player target = otherName == null ? asPlayer(commandSender) : playerHelper.matchPlayerSingle(otherName);


		String materialName = args[0];
		final int colonPos = materialName.indexOf(':');
		String colorName = null;
		if (colonPos >= 0) {
			colorName = materialName.substring(colonPos+1);
			materialName = materialName.substring(0, colonPos);
		}
		final Material material = matchMaterial(materialName);
		if (material == null) {
			if (count > 10)
				count = 10;

			for (int i = 0; i < count; ++i) {
				try {
					plugin.spawnUtils.buildMob(args[0].split("\\+"), commandSender, target, target.getLocation());
				}
				catch (PermissionDeniedException e) {
					throw new YiffBukkitCommandException("Material "+materialName+" not found");
				}
				catch (YiffBukkitCommandException e) {
					playerHelper.sendDirectedMessage(commandSender, "Material "+materialName+" not found");
					throw e;
				}
			}

			playerHelper.sendDirectedMessage(commandSender, "Created "+count+" creatures.");
			return;
		}

		if (material.getId() == 0)
			throw new YiffBukkitCommandException("Material "+materialName+" not found");

		final ItemStack stack = new ItemStack(material, count);

		if (colorName != null) {
			colorName = colorName.toUpperCase();
			Short dataValue = getDataValue(material, colorName);
			if (dataValue != null) {
				stack.setDurability(dataValue);
			}
			else if (material == Material.WOOL || material == Material.INK_SACK) {
				try {
					DyeColor dyeColor = DyeColor.valueOf(colorName.replace("GREY", "GRAY"));

					if (material == Material.WOOL)
						stack.setDurability(dyeColor.getData());
					else
						stack.setDurability((short) (15-dyeColor.getData()));
				}
				catch (IllegalArgumentException e) {
					throw new YiffBukkitCommandException("Color "+colorName+" not found", e);
				}
			}
			else {
				throw new YiffBukkitCommandException("Material "+materialName+" cannot have a data value.");
			}
		}

		PlayerInventory inv = target.getInventory();
		int empty = inv.firstEmpty();
		inv.setItem(empty, stack);

		if (target == commandSender)
			playerHelper.sendDirectedMessage(commandSender, "Item has been put in first free slot of your inventory!");
		else
			playerHelper.sendDirectedMessage(commandSender, "Item has been put in first free slot of "+target.getName()+"'s inventory!");
	}
}
