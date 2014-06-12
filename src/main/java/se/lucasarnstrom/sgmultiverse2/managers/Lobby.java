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

import org.bukkit.Location;
import se.lucasarnstrom.lucasutils.ConsoleLogger;

public class Lobby {

    private final ConsoleLogger logger;
    private       Location      location;

    public Lobby(String name) {
        logger = new ConsoleLogger(name + "-LobbyManager");
        logger.debug("Initiated");
    }

    public void setLocation(Location l) {
        location = l;
    }
}
