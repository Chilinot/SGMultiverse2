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
package se.lucasarnstrom.sgmultiverse2.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
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
}
