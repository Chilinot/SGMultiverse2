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

package se.lucasarnstrom.sgmultiverse2.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.GameWorld;
import se.lucasarnstrom.sgmultiverse2.Language;
import se.lucasarnstrom.sgmultiverse2.Main;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class WorldManager {

    public enum StatusFlag {
		STARTED,
		WAITING,
		FAILED
	}

	private Main plugin;
	private ConsoleLogger logger = new ConsoleLogger("WorldManager");

	private HashMap<String, GameWorld> worlds = new HashMap<>();

	public WorldManager(Main instance) {
		plugin = instance;
		logger.debug("Initiated");
	}

	public void addWorld(World w) {
		worlds.put(w.getName(), new GameWorld(plugin, w));

		final String wname = w.getName();

		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.sqlite.loadLocations(wname);
			}
		}.runTaskAsynchronously(plugin);
	}

	public String[] getRegisteredWorldnames() {
		return worlds.keySet().toArray(new String[worlds.size()]);
	}

	public void setSignLocation(String w, Location l) {
		if(isRegistered(w)) {
			worlds.get(w).setSignLocation(l);
		}
		else {
			logger.severe("Tried to set info-sign for non-registered world!");
		}
	}

	public boolean isRegistered(String w) {
		return worlds.containsKey(w);
	}

	public boolean allowPlayerJoin(String w) {
		return worlds.containsKey(w) && worlds.get(w).allowPlayerJoin();
	}

	public void addPlayer(String worldname, Player p) {
		if (worlds.containsKey(worldname)) {
			worlds.get(worldname).addPlayer(p);
		}
	}

    public void removePlayer(UUID id, Language reason) {

		logger.debug("Removing player with id=\"" + Bukkit.getPlayer(id).getName() + "\" due to reason=\"" + reason + "\"");

        for(Entry<String, GameWorld> e : worlds.entrySet()) {
            if(e.getValue().isInPlayerlist(id)) {
                e.getValue().removePlayer(id);
                broadcast(e.getKey(), reason.getMessage());
                break;
            }
        }
    }

    public boolean isPlaying(UUID id) {
        boolean playing = false;

        for(GameWorld w : worlds.values()) {
            if(w.isInPlayerlist(id)) {
                playing = true;
                break;
            }
        }

        return playing;
    }

	public void broadcast(String wname, String msg) {
		broadcast(Bukkit.getWorld(wname), msg);
	}

	public void broadcast(World w, String msg) {

		logger.debug("Broadcasting msg \"" + msg + "\" to world \"" + w.getName() + "\".");

		msg = "[" + ChatColor.GOLD + "SGM" + ChatColor.WHITE + "] - " + msg;

		for (Player p : w.getPlayers()) {
			p.sendMessage(msg);
		}
	}

	public int getNumberOfMainLocations(String w) {
		return isRegistered(w) ? worlds.get(w).getNumberOfMainLocations() : 0;
	}

	public int getNumberOfArenaLocations(String w) {
		return isRegistered(w) ? worlds.get(w).getNumberOfArenaLocations() : 0;
	}

	public void saveLocations(String w) {
		if (isRegistered(w)) {
			worlds.get(w).saveLocations();
		}
	}

	public void addMainLocation(String w, Location l) {
		if (isRegistered(w)) {
			worlds.get(w).addLocationStart(l);
		}
	}

	public void addArenaLocation(String w, Location l) {
		if (isRegistered(w)) {
			worlds.get(w).addLocationArena(l);
		}
	}

	public boolean allowBlock(Block b) {
		return isRegistered(b.getWorld().getName()) && worlds.get(b.getWorld().getName()).allowBlock(b.getType());
	}

	public void logBlock(Block b, boolean placed) {
		if(isRegistered(b.getWorld().getName())) {
			worlds.get(b.getWorld().getName()).logBlock(b, placed);
		}
	}

	public StatusFlag getStatusFlag(String w) {
		if(isRegistered(w)) {
			return worlds.get(w).getStatus();
		}

		return StatusFlag.FAILED;
	}

	public void logEntity(Hanging e, boolean remove) {
		if(isRegistered(e.getWorld().getName())) {
			worlds.get(e.getWorld().getName()).logEntity(e, remove);
		}
	}
}