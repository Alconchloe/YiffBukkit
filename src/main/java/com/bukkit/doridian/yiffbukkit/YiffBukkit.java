package com.bukkit.doridian.yiffbukkit;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.bukkit.doridian.yiffbukkit.commands.ICommand;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
* YiffBukkit
* @author Doridian
*/
public class YiffBukkit extends JavaPlugin {
    private final YiffBukkitPlayerListener playerListener;
    private final YiffBukkitBlockListener blockListener;
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public PlayerHelper playerHelper = null;
    public final Utils utils;
    public Permissions permissions;
    
    public YiffBukkit(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        playerListener = new YiffBukkitPlayerListener(this);
        blockListener = new YiffBukkitBlockListener(this);
        utils = new Utils(this);
    }

    public void onDisable() {
        System.out.println( "YiffBukkit is disabled!" );
    }

    public void setupPermissions() {
    	permissions = (Permissions)getServer().getPluginManager().getPlugin("Permissions");
    	System.out.println( "Permissions loaded!" );
    }
    
    public void onEnable() {
    	setupPermissions();
    	playerHelper = new PlayerHelper(this);
    	
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);

        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
        //pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
        
        System.out.println( "YiffBukkit is enabled!" );
    }
    
    public Hashtable<String,ICommand> GetCommands() {
    	return playerListener.commands;
    }
    
    public Location TogglePlayerWorlds(Player ply, Location pos) {
    	World world = ToggleWorlds(ply.getWorld());
    	pos.setWorld(world);
    	pos.setZ(world.getHighestBlockYAt(pos.getBlockX(), pos.getBlockZ()));
    	return pos;
    }
    
    public World ToggleWorlds(World world) {
    	if(world.getEnvironment() == Environment.NORMAL)
    		return GetOrCreateWorld(world.getName() + "_nether", Environment.NETHER);
    	else
    		return GetOrCreateWorld(world.getName().substring(0, world.getName().length() - 7), Environment.NORMAL);
    }
    
    public World GetOrCreateWorld(String name, Environment env) {
    	name = name.toLowerCase();
    	List<World> worlds = getServer().getWorlds();
    	for(World world : worlds) {
    		if(world.getName().equals(name)) return world;
    	}
    	World world = getServer().createWorld(name, env);
    	return world;
    }

    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}