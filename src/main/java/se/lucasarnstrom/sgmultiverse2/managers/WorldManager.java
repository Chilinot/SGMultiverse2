package se.lucasarnstrom.sgmultiverse2.managers;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.misc.GameWorld;

public class WorldManager {
	
	private ConsoleLogger logger = new ConsoleLogger("WorldManager");
	
	private HashMap<String, GameWorld> worlds = new HashMap<String, GameWorld>();
	
	public WorldManager() {
		logger.debug("Initiated");
	}
	
	public void addWorld(World w) {
		worlds.put(w.getName(), new GameWorld(w));
	}
	
	public boolean isRegistered(String w) {
		return worlds.containsKey(w);
	}
	
	public boolean allowPlayerJoin(String w) {
		if(worlds.containsKey(w)) {
			return worlds.get(w).allowPlayerJoin();
		}
		
		return false;
	}
	
	public void addPlayer(String worldname, Player p) {
		if(worlds.containsKey(worldname)) {
			worlds.get(worldname).addPlayer(p);
		}
	}

    public void broadcast(String wname, String msg) {
        broadcast(Bukkit.getWorld(wname), msg);
    }
	
	public void broadcast(World w, String msg) {
		
		logger.debug("Broadcasting msg \"" + msg + "\" to world \"" + w.getName() + "\".");
		
		msg = "[" + ChatColor.GOLD + "SGM" + ChatColor.WHITE + "] - " + msg;
		
		for(Player p : w.getPlayers()) {
			p.sendMessage(msg);
		}
	}

	public int getNumberOfMainLocations(String w) {
		if(isRegistered(w)) {
			 return worlds.get(w).getNumberOfMainLocations();
		}
		
		return 0;
	}

	public int getNumberOfArenaLocations(String w) {
		if(isRegistered(w)) {
            return worlds.get(w).getNumberOfArenaLocations();
		}
		
		return 0;
	}

    public void saveLocations(String w) {
        if(isRegistered(w)) {
            worlds.get(w).saveLocations();
        }
    }

    public void addMainLocation(String w, Location l) {
        if(isRegistered(w)) {
            worlds.get(w).addLocationStart(l);
        }
    }

    public void addArenaLocation(String w, Location l) {
        if(isRegistered(w)) {
            worlds.get(w).addLocationArena(l);
        }
    }
}