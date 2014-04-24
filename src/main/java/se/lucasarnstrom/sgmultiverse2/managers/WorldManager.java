package se.lucasarnstrom.sgmultiverse2.managers;

import java.util.HashSet;

import org.bukkit.World;

import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.misc.GameWorld;

public class WorldManager {
	
	private ConsoleLogger logger = new ConsoleLogger("WorldManager");
	
	private HashSet<GameWorld> worlds = new HashSet<GameWorld>();
	
	public WorldManager() {
		logger.debug("Initiated");
	}
	
	public void addWorld(World w) {
		worlds.add(new GameWorld(w));
	}
}
