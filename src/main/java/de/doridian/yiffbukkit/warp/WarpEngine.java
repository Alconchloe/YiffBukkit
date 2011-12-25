package de.doridian.yiffbukkit.warp;

import de.doridian.yiffbukkit.StateContainer;
import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.util.Ini;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class WarpEngine extends StateContainer {
	public Map<String, String> warpMRU = new Hashtable<String, String>(); // TODO!

	private YiffBukkit plugin;
	private Map<String, WarpDescriptor> warps = new Hashtable<String, WarpDescriptor>();

	public WarpEngine(YiffBukkit plugin) {
		this.plugin = plugin;
	}

	@Loader({"warps", "warp"})
	public void LoadWarps() {
		warps.clear();

		Map<String, List<Map<String, List<String>>>> sections = Ini.load("warps.txt");
		if (sections == null)
			return;

		for (Entry<String, List<Map<String, List<String>>>> entry : sections.entrySet()) {
			String warpName = entry.getKey();
			List<Map<String, List<String>>> namesakes = entry.getValue();

			warps.put(warpName.toLowerCase(), new WarpDescriptor(plugin, warpName, namesakes.get(0)));

			if (namesakes.size() > 1) {
				System.err.println("Duplicate section in warps.txt.");
			}
		}
	}

	@Saver({"warps", "warp"})
	public void SaveWarps() {
		Map<String, List<Map<String, List<String>>>> sections = new TreeMap<String, List<Map<String, List<String>>>>();
		for (Entry<String, WarpDescriptor> entry : warps.entrySet()) {
			WarpDescriptor warp = entry.getValue();
			@SuppressWarnings("unchecked")
			final List<Map<String, List<String>>> wrappedSection = Arrays.asList(warp.save());
			sections.put(warp.name, wrappedSection);
		}

		Ini.save("warps.txt", sections);
	}

	public WarpDescriptor setWarp(String ownerName, String name, Location location) throws WarpException {
		if (!Pattern.matches("^[A-Za-z0-9_]+$", name))
			throw new WarpException("Warp name invalid.");

		if (warps.containsKey(name.toLowerCase()))
			throw new WarpException("Warp already exists.");

		WarpDescriptor warp = new WarpDescriptor(plugin, ownerName, name, location);
		warps.put(name.toLowerCase(), warp);
		SaveWarps();

		return warp;
	}

	public WarpDescriptor removeWarp(CommandSender commandSender, String name) throws WarpException {
		WarpDescriptor warp = warps.get(name.toLowerCase());

		if (warp == null)
			throw new WarpException("Warp not found.");

		if (warp.checkAccess(commandSender) < 3)
			throw new WarpException("Permission denied.").setColor('4');

		warps.remove(name.toLowerCase());
		SaveWarps();

		return warp;
	}

	public WarpDescriptor getWarp(CommandSender commandSender, String name) throws WarpException {
		WarpDescriptor warp = warps.get(name.toLowerCase());

		if (warp == null)
			throw new WarpException("Warp not found.");

		if (warp.checkAccess(commandSender) < 1)
			throw new WarpException("Permission denied.").setColor('4');

		return warp;
	}

	public Map<String, WarpDescriptor> getWarps() {
		return new Hashtable<String, WarpDescriptor>(warps);
	}
}
