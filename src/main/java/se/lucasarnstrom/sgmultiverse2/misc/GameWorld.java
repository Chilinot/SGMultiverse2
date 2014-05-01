package se.lucasarnstrom.sgmultiverse2.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.Main;
import se.lucasarnstrom.sgmultiverse2.databases.SQLiteInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

public class GameWorld {

	private Main plugin;
	private ConsoleLogger logger;

	private final World world;
	private HashSet<UUID> playerlist = new HashSet<>();
	private HashMap<Location, String> locations_start = new HashMap<>();
	private HashMap<Location, Boolean> locations_arena = new HashMap<>();

	public GameWorld(Main instance, World w) {
		plugin = instance;
		world = w;
		logger = new ConsoleLogger("GameWorld-" + w.getName());
	}

	public void addLocationStart(Location l) {
		locations_start.put(l, null);
	}

	public void addLocationArena(Location l) {
		locations_arena.put(l, false);
	}

	public int getNumberOfMainLocations() {
		return locations_start.size();
	}

	public int getNumberOfArenaLocations() {
		return locations_arena.size();
	}

	public void sendAllPlayersToArena() {
		Iterator<UUID> i = playerlist.iterator();
		while (i.hasNext()) {

			UUID id = i.next();

			Player p = Bukkit.getPlayer(id);
			if (p == null) {
				logger.severe("Player fetch from UUID returned null player!");
				i.remove();
				continue;
			}

			Location l = null;
			for (Entry<Location, Boolean> e : locations_arena.entrySet()) {
				if (!e.getValue()) {
					l = e.getKey();
					break;
				}
			}

			if (l == null) {
				p.sendMessage(ChatColor.RED + "The arena was full so you have been killed! Sorry about that.");
				p.setHealth(0D);
				i.remove();
				continue;
			}

			if (p.teleport(l)) {
				locations_arena.put(l, true);
			} else {
				logger.severe("Could not teleport a player to the arena!");
			}
		}
	}

	public boolean allowPlayerJoin() {
		return playerlist.size() + 1 % locations_start.size() + 1 > 0; // Added +1 to make sure it never divided by zero.
	}

	public void addPlayer(Player p) {

		logger.debug("Adding player \"" + p.getName() + "\".");

		Location l = null;
		for (Entry<Location, String> e : locations_start.entrySet()) {
			if (e.getValue() == null) {
				l = e.getKey();
				break;
			}
		}

		if (l == null) {
			logger.severe("Tried to add player to already full world!");
			return;
		}

		locations_start.put(l, p.getName());
		playerlist.add(p.getUniqueId());

		//TODO Backup player's inventory

		p.teleport(l);
	}

	public void saveLocations() {
		final String wname = world.getName();

		final Location[] main = (Location[]) locations_start.keySet().toArray();
		final Location[] arena = (Location[]) locations_arena.keySet().toArray();

		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.sqlite.storeStartLocations(wname, SQLiteInterface.LocationType.MAIN, main);
				plugin.sqlite.storeStartLocations(wname, SQLiteInterface.LocationType.ARENA, arena);
			}
		}.runTaskAsynchronously(plugin);
	}
}
