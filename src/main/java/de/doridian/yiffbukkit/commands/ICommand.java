package de.doridian.yiffbukkit.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.YiffBukkitCommandException;
import de.doridian.yiffbukkit.listeners.YiffBukkitPlayerListener;
import de.doridian.yiffbukkit.util.PlayerHelper;

public abstract class ICommand {
	@Retention(RetentionPolicy.RUNTIME) protected @interface Names { String[] value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface Help { String value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface Usage { String value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface Level { int value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface Permission { String value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface Disabled { }
	@Retention(RetentionPolicy.RUNTIME) protected @interface BooleanFlags { String value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface StringFlags { String value(); }
	@Retention(RetentionPolicy.RUNTIME) protected @interface NumericFlags { String value(); }

	public enum FlagType {
		BOOLEAN, STRING, NUMERIC
	}

	private final Map<Character, FlagType> flagTypes = new HashMap<Character, FlagType>();

	protected final Set<Character> booleanFlags = new HashSet<Character>();
	protected final Map<Character, String> stringFlags = new HashMap<Character, String>();
	protected final Map<Character, Double> numericFlags = new HashMap<Character, Double>();

	protected YiffBukkit plugin;
	protected PlayerHelper playerHelper;

	protected ICommand() {
		this(YiffBukkitPlayerListener.instance);
	}
	private ICommand(YiffBukkitPlayerListener playerListener) {
		plugin = playerListener.plugin;
		playerHelper = plugin.playerHelper;

		if (this.getClass().getAnnotation(Disabled.class) != null)
			return;

		Names namesAnnotation = this.getClass().getAnnotation(Names.class);
		if (namesAnnotation != null) {
			for (String name : namesAnnotation.value()) {
				playerListener.registerCommand(name, this);
			}
		}

		parseFlagsAnnotations();
	}

	private void parseFlagsAnnotations() {
		BooleanFlags booleanFlagsAnnotation = this.getClass().getAnnotation(BooleanFlags.class);
		if (booleanFlagsAnnotation != null) {
			parseFlagsAnnotation(booleanFlagsAnnotation.value(), FlagType.BOOLEAN);
		}

		StringFlags stringFlagsAnnotation = this.getClass().getAnnotation(StringFlags.class);
		if (stringFlagsAnnotation != null) {
			parseFlagsAnnotation(stringFlagsAnnotation.value(), FlagType.STRING);
		}

		NumericFlags numericFlagsAnnotation = this.getClass().getAnnotation(NumericFlags.class);
		if (numericFlagsAnnotation != null) {
			parseFlagsAnnotation(numericFlagsAnnotation.value(), FlagType.NUMERIC);
		}
	}

	private void parseFlagsAnnotation(final String flags, final FlagType flagType) {
		for (int i = 0; i < flags.length(); ++i) {
			flagTypes.put(flags.charAt(i), flagType);
		}
	}

	protected String parseFlags(String argStr) throws YiffBukkitCommandException {
		if (argStr.trim().isEmpty()) {
			booleanFlags.clear();
			stringFlags.clear();
			numericFlags.clear();
			return argStr;
		}

		String[] args = argStr.split(" ");

		args = parseFlags(args);

		if (args.length == 0)
			return "";

		StringBuilder sb = new StringBuilder(args[0]);
		for (int i = 1; i < args.length; ++i) {
			sb.append(' ');
			sb.append(args[i]);
		}

		return sb.toString();
	}

	protected String[] parseFlags(String[] args) throws YiffBukkitCommandException {
		int nextArg = 0;

		parseFlagsAnnotations();
		booleanFlags.clear();
		stringFlags.clear();
		numericFlags.clear();

		while (nextArg < args.length) {
			// Fetch argument
			String arg = args[nextArg++];

			// Empty argument? (multiple consecutive spaces)
			if (arg.isEmpty())
				continue;

			// No more flags?
			if (arg.charAt(0) != '-' || arg.length() == 1) {
				--nextArg;
				break;
			}

			// Handle flag parsing terminator --
			if (arg.equals("--"))
				break;

			if (!Character.isLetter(arg.charAt(1))) {
				--nextArg;
				break;
			}

			// Go through the flags
			for (int i = 1; i < arg.length(); ++i) {
				char flagName = arg.charAt(i);

				final FlagType flagType = flagTypes.get(flagName);
				if (flagType == null)
					throw new YiffBukkitCommandException("Invalid flag '"+flagName+"' specified.");

				switch (flagType) {
				case BOOLEAN:
					booleanFlags.add(flagName);
					break;

				case STRING:
					// Skip empty arguments...
					while (nextArg < args.length && args[nextArg].isEmpty())
						++nextArg;

					if (nextArg >= args.length)
						throw new YiffBukkitCommandException("No value specified for "+flagName+" flag.");

					stringFlags.put(flagName, args[nextArg++]);
					break;

				case NUMERIC:
					// Skip empty arguments...
					while (nextArg < args.length && args[nextArg].isEmpty())
						++nextArg;

					if (nextArg >= args.length)
						throw new YiffBukkitCommandException("No value specified for "+flagName+" flag.");

					numericFlags.put(flagName, Double.parseDouble(args[nextArg++]));
					break;
				}
			}
		}

		return Arrays.copyOfRange(args, nextArg, args.length);
	}

	public final int getMinLevel() {
		Level levelAnnotation = this.getClass().getAnnotation(Level.class);
		if (levelAnnotation == null)
			throw new UnsupportedOperationException("You need either a GetMinLevel method or an @Level annotation.");

		return levelAnnotation.value();
	}

	public void Run(Player ply, String[] args, String argStr) throws YiffBukkitCommandException {

	}
	public void run(CommandSender commandSender, String[] args, String argStr) throws YiffBukkitCommandException {

		Run(asPlayer(commandSender), args, argStr);
	}

	public static Player asPlayer(CommandSender commandSender) throws YiffBukkitCommandException {
		if (!(commandSender instanceof Player))
			throw new YiffBukkitCommandException("This command can only be run as a player.");

		return (Player)commandSender;
	}

	public final String getHelp() {
		Help helpAnnotation = this.getClass().getAnnotation(Help.class);
		if (helpAnnotation == null)
			return "";

		return helpAnnotation.value();
	}
	public final String getUsage() {
		Usage usageAnnotation = this.getClass().getAnnotation(Usage.class);
		if (usageAnnotation == null)
			return "";

		return usageAnnotation.value();
	}

	public boolean canPlayerUseCommand(CommandSender commandSender)
	{
		Permission permissionAnnotation = this.getClass().getAnnotation(Permission.class);
		if (permissionAnnotation != null)
			return plugin.permissionHandler.has(commandSender, permissionAnnotation.value());

		int plylvl = plugin.playerHelper.getPlayerLevel(commandSender);
		int reqlvl = getMinLevel();

		return plylvl >= reqlvl;
	}
}
