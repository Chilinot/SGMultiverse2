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

package se.lucasarnstrom.sgmultiverse2;

import me.desht.dhutils.block.CraftMassBlockUpdate;
import me.desht.dhutils.block.MassBlockUpdate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.databases.SQLiteInterface.LocationType;
import se.lucasarnstrom.sgmultiverse2.logging.LoggedBlock;
import se.lucasarnstrom.sgmultiverse2.logging.LoggedEntity;
import se.lucasarnstrom.sgmultiverse2.managers.WorldManager.StatusFlag;

import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("deprecation")
public class GameWorld {

    private final Main          plugin;
    private final ConsoleLogger logger;
    private final World         world;
    private       Location      lobby;
    private       Location                       sign_location      = null;
    private final HashSet<UUID>                  playerlist         = new HashSet<>();
    private final HashMap<Location, UUID>        locations_start    = new HashMap<>();
    private final HashMap<Location, Boolean>     locations_arena    = new HashMap<>();
    private       EnumSet<Material>              blockfilter        = null;
    private final HashMap<Location, LoggedBlock> log_block          = new HashMap<>();
    private final HashMap<UUID, LoggedEntity>    log_entity         = new HashMap<>();
    private final HashSet<Entity>                log_entity_removal = new HashSet<>();
    private       boolean                        inReset            = false;
    private       StatusFlag                     status             = StatusFlag.WAITING_FOR_PLAYERS;

    // Entities that shouldn't be removed on world reset
    private static final EnumSet<EntityType> nonremovable = EnumSet.of(
            EntityType.PLAYER,
            EntityType.PAINTING,
            EntityType.ITEM_FRAME
    );

    public GameWorld(Main instance, World w) {
        plugin = instance;
        world = w;
        logger = new ConsoleLogger("GameWorld-" + w.getName());

        //TODO Load all configurations for the world.

        // Load blockfilter
        String materials = plugin.getConfig().getString("worlds." + world.getName() + ".blockfilter");

        if(materials != null) {
            String[] list = materials.split(", ");

            for(String s : list) {

                if(s.equalsIgnoreCase("false")) {
                    logger.info("Blockfilter disabled for world " + world.getName());
                    blockfilter = null; // Just to make sure it is disabled.
                    break;
                }

                // Remove any data added by user since it can't handle that right now
                if(s.contains(":"))
                    s = s.split(":")[0];

                try {
                    int id = Integer.parseInt(s);
                    addMaterialToFilter(id);
                }
                catch(NumberFormatException e) {
                    logger.severe("Incorrectly formatted blockfilter for world \"" + world.getName() + "\" :: ENTRY IS NOT A VALID MATERIAL-ID: ENTRY = \"" + s + "\"");
                    continue;
                }
            }
        }

        loadSign();
    }

    public void setSignLocation(final Location l, boolean save) {
        sign_location = l;

        if(save) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.sqlite.storeSignLocation(world.getName(), l);
                }
            }.runTaskAsynchronously(plugin);
        }

        // Update the sign on the next tick because it was used in the SignChangeEvent.
        new BukkitRunnable() {
            @Override
            public void run() {
                updateSign();
            }
        }.runTask(plugin);
    }

    public void setLobbyLocation(final Location l, boolean save) {

        lobby = l;

        if(save) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.sqlite.storeLobbyLocation(world.getName(), l);
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    private void updateSign() {

        logger.debug("Updating info-sign.");

        if(sign_location == null) {
            logger.debug("Sign_location is null, attempting to load sign from database.");
            loadSign(); // This could possibly create an infinite loop if something weird would happen.
            return;
        }

        Material m = sign_location.getBlock().getType();
        if(m == Material.SIGN_POST || m == Material.WALL_SIGN) {

            Sign s = (Sign) sign_location.getBlock().getState();

            StringBuilder status = new StringBuilder();

            switch(this.status) {
                case STARTED:
                    status.append(ChatColor.GOLD);
                    break;
                case WAITING_FOR_PLAYERS:
                    status.append(ChatColor.GREEN);
                    break;
                case FAILED:
                    status.append(ChatColor.RED);
                    break;
            }

            status.append(this.status);

            s.setLine(0, ChatColor.WHITE + world.getName());
            s.setLine(1, status.toString());
            s.setLine(2, ChatColor.WHITE + "-PLAYERS-");
            s.setLine(3, ChatColor.WHITE + Integer.toString(playerlist.size()) + // It kept confusing the string concatenation with integer addition.
                    ChatColor.GOLD + "/" +
                    ChatColor.WHITE + getNumberOfMainLocations());

            s.update();
        }
        else {
            logger.severe("The location for this worlds info-sign is no longer a sign!");
        }
    }

    private void loadSign() {

        final String wname = world.getName();

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.sqlite.loadSignLocation(wname);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void addMaterialToFilter(int id) throws NumberFormatException {
        if(blockfilter == null) {
            blockfilter = EnumSet.noneOf(Material.class);
        }

        Material m = Material.getMaterial(id);

        if(m == null)
            throw new NumberFormatException();
        else {
            logger.debug("Adding material \"" + m + "\" to blockfilter for world \"" + world.getName() + "\"");
            blockfilter.add(m);
        }
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

        plugin.worldManager.broadcast(world, Language.TELEPORT_ARENA);

        Iterator<UUID> i = playerlist.iterator();
        while(i.hasNext()) {

            UUID id = i.next();

            Player p = Bukkit.getPlayer(id);
            if(p == null) {
                logger.severe("Player fetch from UUID returned null player!");
                i.remove();
                continue;
            }

            Location l = null;
            for(Entry<Location, Boolean> e : locations_arena.entrySet()) {
                if(!e.getValue()) {
                    l = e.getKey();
                    break;
                }
            }

            if(l == null) {
                p.sendMessage(ChatColor.RED + "The arena was full so you have been killed! Sorry about that.");
                p.setHealth(0D);
                i.remove();
                continue;
            }

            if(p.teleport(l)) {
                locations_arena.put(l, true);
            }
            else {
                logger.severe("Could not teleport a player to the arena!");
            }
        }
    }

    public boolean allowPlayerJoin() {
        return (playerlist.size() + 1) % (locations_start.size() + 1) > 0; // Added +1 to make sure it never divided by zero.
    }

	/*public void addPlayer(Player p) {

		if(!allowPlayerJoin()) {
			logger.severe("Tried to add player to already full world!");
			return;
		}
		else if(lobby == null) {
			logger.severe("The lobby has not been set for this world!");
			p.sendMessage(ChatColor.RED + "You just tried to enter a incorrectly configured world! " +
					"Please contact the server admin!");
			return;
		}

		logger.debug("Adding player \"" + p.getName() + "\".");

		Location l = null;
		for (Entry<Location, UUID> e : locations_start.entrySet()) {
			if (e.getValue() == null) {
				l = e.getKey();
				break;
			}
		}

		if (l == null) {
			logger.severe("Tried to add player to already full world!");
			return;
		}

		locations_start.put(l, p.getUniqueId());

		p.teleport(lobby); // The player has to be teleported before he/she is added to the playerlist.

		playerlist.add(p.getUniqueId());

		//TODO Backup player's inventory
		//TODO check if start game
		//TODO Create all time related classes.

		updateSign();
	}*/

    public void removePlayer(UUID id) {

        if(status == StatusFlag.COUNTINGDOWN) { //TODO recheck if enough players!
            for(Entry<Location, UUID> e : locations_start.entrySet()) {
                if(e.getValue().equals(id)) {
                    e.setValue(null);
                    break;
                }
            }
        }

        playerlist.remove(id);
        updateSign();
        //TODO Restore the players inventory if he/she is still online.
    }

    public boolean isInPlayerlist(UUID id) {
        return playerlist.contains(id);
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
                plugin.sqlite.storeStartLocations(wname, LocationType.MAIN, start);
                plugin.sqlite.storeStartLocations(wname, LocationType.ARENA, arena);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void reset() {
        inReset = true;

        try {
            // Restore the world
            logger.debug("Resetting world: " + world.getName());

            MassBlockUpdate mbu = CraftMassBlockUpdate.createMassBlockUpdater(plugin, world);

            mbu.setRelightingStrategy(MassBlockUpdate.RelightingStrategy.NEVER);

            for(LoggedBlock block : log_block.values()) {
                block.reset(mbu);
            }

            for(LoggedEntity entity : log_entity.values()) {
                if(entity != null)
                    entity.reset();
            }

            // Clearing entities seems to be more efficient if it is run at the next tick.
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                public void run() {
                    clearEntities();
                }
            });

            log_block.clear();
            log_entity.clear();

            // Reset location statuses
            for(Entry<Location, UUID> e : locations_start.entrySet()) {
                e.setValue(null);
            }
            for(Entry<Location, Boolean> e : locations_arena.entrySet()) {
                e.setValue(false);
            }

            updateSign();
        }
        finally {
            inReset = false;
        }
    }

    public void clearEntities() {

        // Clear all logged entities
        for(Entity e : log_entity_removal) {
            if(e != null)
                e.remove();
        }

        log_entity_removal.clear();

        // Clear the remaining basic entities
        for(Entity entity : world.getEntities()) {
            if(nonremovable.contains(entity.getType()))
                continue;
            entity.remove();
        }
    }

    public void logBlock(Block b, boolean placed) {
        if(inReset)
            return;

        Location l = b.getLocation();

        if(!log_block.containsKey(l)) {

            Material material = placed ? Material.AIR : b.getType();

            String[] sign_lines = null;

            if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                sign_lines = ((Sign) b.getState()).getLines();
            }

            log_block.put(l, new LoggedBlock(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), material, b.getData(), sign_lines));
            //logger.debug("Logging block :: " + b.getWorld().getName() + " " + b.getX() + " " + b.getY() + " " + b.getZ() + " " + material + " " + b.getData() + " " + sign_lines);
        }
    }

    public void logEntity(Entity e, boolean remove) {
        if(inReset)
            return;

        if(!log_entity.containsKey(e.getUniqueId())) {
            if(remove) {
                log_entity_removal.add(e);
                log_entity.put(e.getUniqueId(), null);
            }
            else {
                log_entity.put(e.getUniqueId(), new LoggedEntity(e));
            }
            logger.debug("Logged entity " + e.getType() + " " + e.getLocation());
        }
    }

    public StatusFlag getStatus() {
        return status;
    }

    public boolean allowBlock(Material m) {
        return blockfilter == null || blockfilter.contains(m);
    }
}
