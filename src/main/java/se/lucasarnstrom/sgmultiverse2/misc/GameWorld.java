package se.lucasarnstrom.sgmultiverse2.misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import se.lucasarnstrom.lucasutils.ConsoleLogger;

public class GameWorld {
	
	private ConsoleLogger logger;
	
	private final World               world;
	private HashSet<String>           playerlist = new HashSet<String>();
	private HashMap<Location, String> locations  = new HashMap<Location, String>();
	
	public GameWorld(World w) {
		world = w;
		logger = new ConsoleLogger("GameWorld-" + w.getName());
	}

	public boolean allowPlayerJoin() {
		return playerlist.size() + 1 % locations.size() + 1 > 0; // Added +1 to make sure it never divided by zero.
	}

	public void addPlayer(Player p) {
		
		logger.debug("Adding player \"" + p.getName() + "\".");
		
		Location l = null;
		for(Entry<Location, String> e : locations.entrySet()) {
			if(e.getValue() == null) {
				l = e.getKey();
				break;
			}
		}
		
		if(l == null) {
			logger.severe("Tried to add player to already full world! Worldname=\"" + world.getName() + "\"");
			return;
		}
		
		locations.put(l, p.getName());
		playerlist.add(p.getName());
		
		//TODO Backup player's inventory
		
		p.teleport(l);
		
	}
}
