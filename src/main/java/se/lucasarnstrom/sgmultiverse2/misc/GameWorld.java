/**
 *  Author:  Lucas Arnström - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
 *
 *
 *  Copyright 2014 Lucas Arnström
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *
 *  Filedescription:
 *
 *
 */

package se.lucasarnstrom.sgmultiverse2.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.Main;
import se.lucasarnstrom.sgmultiverse2.databases.SQLiteInterface.LocationType;
import se.lucasarnstrom.sgmultiverse2.managers.WorldManager.StatusFlag;

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

		//TODO Load all configurations for the world.
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

		plugin.worldManager.broadcast(world, ChatColor.GOLD + "Sending all players to the arena!");

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

		//TODO check if start game
		//TODO Create all time related classes.
	}

	public void saveLocations() {
		final String wname = world.getName();

		final Location[] start = locations_start.keySet().toArray(new Location[locations_start.size()]);
		final Location[] arena = locations_arena.keySet().toArray(new Location[locations_arena.size()]);

		new BukkitRunnable() {
			@Override
			public void run() {
				// Clear
				plugin.sqlite.clearStartLocations(wname, LocationType.MAIN);
				plugin.sqlite.clearStartLocations(wname, LocationType.ARENA);

				// Save
				plugin.sqlite.storeStartLocations(wname, LocationType.MAIN,  start);
				plugin.sqlite.storeStartLocations(wname, LocationType.ARENA, arena);
			}
		}.runTaskAsynchronously(plugin);
	}

	public boolean allowBlock(Block b) {
		//TODO fix allowBlock
		return true;
	}

	public void logBlock(Block b, boolean placed) {
		//TODO fix logBlock
	}

	public StatusFlag getStatus() {
		//TODO fix getStatus
		return StatusFlag.FAILED;
	}

	public void logEntity(Hanging e, boolean remove) {
		//TODO fix logEntity
	}
}
