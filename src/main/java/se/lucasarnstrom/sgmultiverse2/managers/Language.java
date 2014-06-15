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
 */

package se.lucasarnstrom.sgmultiverse2.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import se.lucasarnstrom.lucasutils.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum Language {

    COMMAND_ERROR_NOTAPLAYER("&cYou have to be a player to use this command!"),
    COMMAND_ERROR_MISSINGARGUMENTS("&cYou have to provide arguments for this command!"),

    COMMAND_SGLOCATION_ERROR_NOTINREGISTEREDWORLD("&cYou are not in a registered gameworld!"),

    COMMAND_SGQUEUE_ERROR_NOTINLOBBY("&cYou have to be in a lobby to use this command!"),

    COMMAND_SGINFO_INFO_MAIN("&2SGMultiverse2 version &6#INFO#&2 is up and running!"),
    COMMAND_SGINFO_INFO_REGISTERED("Currently these worlds are registered as gameworlds:"),
    COMMAND_SGINFO_INFO_REGISTEREDWORLD(" - &6#INFO#"),

    COMMAND_SGJOIN_ERROR_FULLWORLD("&cThat game is full!"),
    COMMAND_SGJOIN_ERROR_NOTAGAME("&cThere is no game with that name!"),
    COMMAND_SGJOIN_MENU_TITLE("Choose a world!"),
    COMMAND_SGJOIN_MENU_CHOICE("Click this to join the game &6#INFO#"),

    COMMAND_SGTP_ERROR_NOTAVALIDWORLD("&cThere is no world with that name on this server!"),
    COMMAND_SGTP_INFO_SENDING("Sending you to &6#INFO#&f!"),

    GAME_NOTSTARTED("&cThe game has not started yet!"),
    GAME_FULLARENA("&cThe arena was full so you have been killed! Sorry about that."),

    LISTENER_BLOCKS_NOTALLOWEDBREAK("&cYou are not allowed to break this block!"),
    LISTENER_BLOCKS_NOTALLOWEDPLACE("&cYou are not allowed to place this block!"),

    LOBBY_ADDED_QUEUE("You have been added to the queue! There are &6#AMOUNT#&f players ahead of you."),
	LOBBY_WELCOME("You are now in the lobby. Please use the command &6/sgqueue&f to start playing!"),

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
    public String INFO   = null;

    private static       FileConfiguration config = null;
    private static final ConsoleLogger     logger = new ConsoleLogger("Language");

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
            msg = config.getString(this.name().replaceAll("_", "\\."), msg);
        }

        // -- Variables
        // "REFLECTION!?!? Are you nuts?" You might say. Yes I might be, but boy do I love dynamic code.
        // If it was possible to define a string variable in a single place that's what I would do, with no care
        // regarding performance loss.
        Field[] fa = Language.class.getDeclaredFields();
        for(Field f : fa) {
            try {
                Object o = f.get(this);
                if(Modifier.isPublic(f.getModifiers()) && o != null && o instanceof String) {
                    msg = msg.replace('#' + f.getName() + '#', (String) o);
                }
            }
            catch(IllegalAccessException e) {
                logger.severe("Failed to replace variable \"" + f.getName() + "\"");
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