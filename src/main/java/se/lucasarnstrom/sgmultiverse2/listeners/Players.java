/**
 *  Author:  Lucas Arnstr�m - LucasEmanuel @ Bukkit forums
 *  Contact: lucasarnstrom(at)gmail(dot)com
 *
 *
 *  Copyright 2014 Lucas Arnstr�m
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
package se.lucasarnstrom.sgmultiverse2.listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.managers.WorldManager.RemoveReason;
import se.lucasarnstrom.sgmultiverse2.Main;

public class Players implements Listener {

	private Main plugin;
	private ConsoleLogger logger = new ConsoleLogger("PlayerListener");

	public Players(Main instance) {
		plugin = instance;
		logger.debug("Initiated");
	}

	@EventHandler
	public void signChange(SignChangeEvent e) {
		String s = e.getLine(0);
		if(plugin.worldManager.isRegistered(s) && e.getPlayer().hasPermission("sgmultiverse.signs.sginfo")) {
			plugin.worldManager.setSignLocation(s, e.getBlock().getLocation());
		}
	}

    @EventHandler
    public void playerInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                e.getClickedBlock().getType() == Material.CHEST &&
                plugin.worldManager.isRegistered(e.getPlayer().getWorld().getName())) {

            plugin.chestManager.randomizeChest((Chest) e.getClickedBlock().getState());
        }
    }

	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {

        logger.debug("Cought PlayerDeathEvent");

		if(plugin.worldManager.isPlaying(e.getEntity().getUniqueId())) {

			Player k = e.getEntity().getKiller();

			if(k == null) {
				RemoveReason reason = RemoveReason.DEATH;
				reason.setPlayer(e.getEntity().getName());
				plugin.worldManager.removePlayer(e.getEntity().getUniqueId(), reason);
			}
			else {
				RemoveReason reason = RemoveReason.KILLED;
				reason.setPlayer(e.getEntity().getName());
				reason.setKiller(k.getName());
				plugin.worldManager.removePlayer(e.getEntity().getUniqueId(), reason);
			}

			e.setDeathMessage(null);
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		//TODO restore inventory
	}

    @EventHandler
    public void playerTeleport(PlayerTeleportEvent e) {
        if(plugin.worldManager.isPlaying(e.getPlayer().getUniqueId())
                && !e.getTo().getWorld().equals(e.getFrom().getWorld())) {
            plugin.worldManager.removePlayer(e.getPlayer().getUniqueId(), RemoveReason.TELEPORT);
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        if(plugin.worldManager.isPlaying(e.getPlayer().getUniqueId())) {
            plugin.worldManager.removePlayer(e.getPlayer().getUniqueId(), RemoveReason.QUIT);
        }
    }

    @EventHandler
    public void playerKick(PlayerKickEvent e) {
        if(plugin.worldManager.isPlaying(e.getPlayer().getUniqueId())) {
            plugin.worldManager.removePlayer(e.getPlayer().getUniqueId(), RemoveReason.KICK);
        }
    }
}
