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

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import se.lucasarnstrom.lucasutils.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public enum Language {

    LOBBY_ADDED_QUEUE("You have been added to the queue! There are &6#AMOUNT#&f players ahead of you."),

    PLAYER_KICK("&6#PLAYER#&f has been kicked from the game!"),
    PLAYER_QUIT("&6#PLAYER#&f left the game."),
    PLAYER_TELEPORT("&6#PLAYER#&f teleported out of this world and was removed!"),
    PLAYER_DEATH("&6#PLAYER#&f was killed."),
    PLAYER_KILLED("&6#PLAYER#&f was killed by &6#KILLER#&f!"),

    TELEPORT_ARENA("&6Sending all players to the arena!");

    private String msg;

    // Variable-fields
    public String PLAYER = null;
    public String KILLER = null;
    public String AMOUNT = null;

    // It will only access variable-fields defined in this enum.
    private enum Variable {
        PLAYER,
        KILLER,
        AMOUNT
    }

    private static FileConfiguration config = null;
    private static ConsoleLogger     logger = new ConsoleLogger("Language");

    private Language(String s) {
        msg = s;
    }

    public static void setConfig(File file) {

        config = YamlConfiguration.loadConfiguration(file);

        // Store the defaults in the config.
        boolean save = false;

        for(Language l : Language.values()) {
            String key = l.name().replaceAll("_", "\\."); // Transform the strings from "FOO_BAR" to "FOO.BAR"
            if(!config.contains(key)) {
                config.set(key, l.msg);
                save = true;
            }
        }

        if(save) {
            try {
                config.save(file);
            }
            catch(IOException e) {
                logger.severe("Error while trying to save defaults to the config!");
                e.printStackTrace();
            }
        }
    }

    public String getMessage() {

        if(config != null) {
            msg = config.getString(this.name().replaceAll("_", "\\."));
        }

        // -- Variables
        // "REFLECTION!?!? Are you nuts?" You might say. Yes I might be, but boy do I love dynamic code.
        // If it was possible to define a string variable in a single place that's what I would do, with no care
        // regarding performance loss.
        for(Variable v : Variable.values()) {
            try {
                Field f = Language.class.getDeclaredField(v.name());
                Object o = f.get(this);
                if(o != null && o instanceof String) {
                    msg = msg.replace('#' + v.name() + '#', (String) o);
                }
            }
            catch(NoSuchFieldException | IllegalAccessException e) {
                logger.severe("Failed to replace variable \"" + v.name() + "\"");
                logger.severe("Message: \"" + e.getMessage() + "\"");
                logger.debug(e.getStackTrace());
            }
        }

        logger.debug("Returning message: \"" + msg + "\"");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String toString() {
        return getMessage();
    }
}