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
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import se.lucasarnstrom.lucasutils.ConsoleLogger;
import se.lucasarnstrom.sgmultiverse2.Main;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChestManager {

	private final Main plugin;
	private final ConsoleLogger logger = new ConsoleLogger("ChestManager");
	private final ArrayList<Location> randomizedchests = new ArrayList<>();
	private final HashMap<String, Object[]> worlds = new HashMap<>();
	private final Random generator = new Random(System.currentTimeMillis());

	public ChestManager(Main instance) {
		plugin = instance;
		logger.debug("Initiated");
	}

	public void addWorld(String name) {

		String config =
				plugin.getDataFolder() +
						"/lootlists/" +
						plugin.getConfig().getString("worlds." + name + ".lootlist") +
						".yml";

		worlds.put(name, new Object[]{
				config,                                           // Itemconfig
				new RandomCollection<String>(),                   // Itemlist
				new HashMap<String, RandomCollection<String>>(),  // enchantmentlists
				new HashMap<String, String>()                     // enchable
		});

		loadItemList(name);
	}

	@SuppressWarnings("unchecked")
	public void randomizeChest(Chest chest) {

		String worldname = chest.getLocation().getWorld().getName();

		if (!randomizedchests.contains(chest.getLocation()) // Is the chest already logged?
				&& worlds.containsKey(worldname)) {        // Is it a registered world?

			FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(
					new File((String) worlds.get(worldname)[0])
			);

			RandomCollection<String> itemlist = (RandomCollection<String>) worlds.get(worldname)[1];
			HashMap<String, String> enchable = (HashMap<String, String>) worlds.get(worldname)[3];
			HashMap<String, RandomCollection<String>> enchantmentlists =
					(HashMap<String, RandomCollection<String>>) worlds.get(worldname)[2];

			int spawnchance = itemConfig.getInt("blankChestChance-OneOutOf");

			if (spawnchance > generator.nextInt(spawnchance + 1)) {

				Inventory inventory = chest.getInventory();
				inventory.clear();

				int items = generator.nextInt(itemConfig.getInt("maxAmountOfItems")) + 1;

				for (int i = 0; i < items; i++) {

					Enchantment enchantment = null;
					String itemname = itemlist.next();

					ItemStack item = new ItemStack(Material.getMaterial(itemname.toUpperCase()), 1);
					
					
					/*
					 *  Enchantments -- start
					 */

					if (enchable.containsKey(itemname)) {

						String itemtype = enchable.get(itemname);

						// Get the specified enchantchance for the item
						double enchantchance = 0.0d;

						if (itemtype.equals("swords") || itemtype.equals("bow")) {
							enchantchance = itemConfig.getDouble("weapons." + itemname + ".enchantmentchance");
						} else if (itemtype.equals("armors")) {
							enchantchance = itemConfig.getDouble("armors." + itemname + ".enchantmentchance");
						}

						// Generate a random double and retrieve an enchantment if the generated value is less or equal to the enchantchance
						if (generator.nextDouble() <= enchantchance) {

							// Try to find a random enchantment with a maximum of 5 tries
							for (int j = 0; j < 5; j++) {
								enchantment = Enchantment.getByName(enchantmentlists.get(itemtype).next().toUpperCase());

								if (enchantment.canEnchantItem(item))
									break;

								enchantment = null;
							}
						}

						// If we have an enchantment, enchant the item
						if (enchantment != null) {

							// Generate a random level for the enchantment based on the items maxlevel + 1
							int level = generator.nextInt(enchantment.getMaxLevel()) + 1;

							// If the level is above the maxlevel, set it to max level
							if (level > enchantment.getMaxLevel())
								level = enchantment.getMaxLevel();
								// If the level is beneath or equal to zero, set it to level 1
							else if (level <= 0)
								level = 1;

							item.addEnchantment(enchantment, level);
						}
					}
					
					/*
					 *  Enchantments -- stop
					 */


					// Place the item in a random slot of the inventory, get a new slot if the previous one where occupied
					int place = 0;

					for (int j = 0; j < inventory.getSize(); j++) {

						place = generator.nextInt(inventory.getSize());

						if (inventory.getItem(place) == null)
							break;
					}

					inventory.setItem(place, item);
				}

				chest.update();
			}

			randomizedchests.add(chest.getLocation());
		}
	}

	public void addChestToLog(Location location) {
		if (!randomizedchests.contains(location))
			randomizedchests.add(location);
	}

	public void clearLogs(String worldname) {
		logger.debug("Clearing logs for world - " + worldname);

		Iterator<Location> locations = randomizedchests.iterator();
		while (locations.hasNext()) {

			Location location = locations.next();

			if (location.getWorld().getName().equals(worldname))
				locations.remove();
		}

		logger.debug("finished...");
	}

	@SuppressWarnings("unchecked")
	private void loadItemList(String worldname) {

		logger.debug("Loading configuration file for world \"" + worldname + "\".");

		FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(
				new File((String) worlds.get(worldname)[0])
		);

		// Check the default settings
		checkDefaults(itemConfig, worldname);

		// Retrieve the needed collections for the world.
		RandomCollection<String> itemlist = (RandomCollection<String>) worlds.get(worldname)[1];
		HashMap<String, String> enchable = (HashMap<String, String>) worlds.get(worldname)[3];
		HashMap<String, RandomCollection<String>> enchantmentlists =
				(HashMap<String, RandomCollection<String>>) worlds.get(worldname)[2];


		// --- Items

		for (String string : itemConfig.getConfigurationSection("weapons").getKeys(false)) {
			itemlist.add(itemConfig.getDouble("weapons." + string + ".spawnchance"), string);

			if (string.equals("bow"))
				enchable.put(string, "bow");
			else
				enchable.put(string, "swords");
		}

		for (String string : itemConfig.getConfigurationSection("armors").getKeys(false)) {
			itemlist.add(itemConfig.getDouble("armors." + string + ".spawnchance"), string);
			enchable.put(string, "armors");
		}

		for (String string : itemConfig.getConfigurationSection("food").getKeys(false)) {
			itemlist.add(itemConfig.getDouble("food." + string), string);
		}

		for (String string : itemConfig.getConfigurationSection("items").getKeys(false)) {
			itemlist.add(itemConfig.getDouble("items." + string), string);
		}


		// --- Enchantments

		RandomCollection<String> swords = new RandomCollection<>();
		RandomCollection<String> bows = new RandomCollection<>();
		RandomCollection<String> armors = new RandomCollection<>();

		for (String swordench : itemConfig.getConfigurationSection("enchantments.swords").getKeys(false)) {
			swords.add(itemConfig.getDouble("enchantments.swords." + swordench), swordench);
		}

		for (String bowench : itemConfig.getConfigurationSection("enchantments.bow").getKeys(false)) {
			bows.add(itemConfig.getDouble("enchantments.bow." + bowench), bowench);
		}

		for (String armorench : itemConfig.getConfigurationSection("enchantments.armors").getKeys(false)) {
			armors.add(itemConfig.getDouble("enchantments.armors." + armorench), armorench);
		}

		enchantmentlists.put("swords", swords);
		enchantmentlists.put("bow", bows);
		enchantmentlists.put("armors", armors);


		logger.debug("Finished loading config");
	}

	private void checkDefaults(FileConfiguration itemConfig, String worldname) {
		boolean save = false;

		if (!itemConfig.contains("maxAmountOfItems")) {
			itemConfig.set("maxAmountOfItems", 3);
			save = true;
		}
		if (!itemConfig.contains("blankChestChance-OneOutOf")) {
			itemConfig.set("blankChestChance-OneOutOf", 5);
			save = true;
		}

		if (!itemConfig.contains("weapons")) {
			itemConfig.set("weapons.wood_spade.enchantmentchance", 0.0);
			itemConfig.set("weapons.wood_spade.spawnchance", 0.5);
			itemConfig.set("weapons.wood_pickaxe.enchantmentchance", 0.0);
			itemConfig.set("weapons.wood_pickaxe.spawnchance", 0.7);
			itemConfig.set("weapons.wood_axe.enchantmentchance", 0.0);
			itemConfig.set("weapons.wood_axe.spawnchance", 0.4);
			itemConfig.set("weapons.stone_sword.enchantmentchance", 0.1);
			itemConfig.set("weapons.stone_sword.spawnchance", 0.3);
			itemConfig.set("weapons.stone_spade.enchantmentchance", 0.0);
			itemConfig.set("weapons.stone_spade.spawnchance", 0.4);
			itemConfig.set("weapons.stone_pickaxe.enchantmentchance", 0.0);
			itemConfig.set("weapons.stone_pickaxe.spawnchance", 0.3);
			itemConfig.set("weapons.gold_sword.enchantmentchance", 0.4);
			itemConfig.set("weapons.gold_sword.spawnchance", 0.2);
			itemConfig.set("weapons.gold_axe.enchantmentchance", 0.0);
			itemConfig.set("weapons.gold_axe.spawnchance", 0.2);
			itemConfig.set("weapons.wood_sword.enchantmentchance", 0.3);
			itemConfig.set("weapons.wood_sword.spawnchance", 0.8);
			itemConfig.set("weapons.iron_sword.enchantmentchance", 0.1);
			itemConfig.set("weapons.iron_sword.spawnchance", 0.1);
			itemConfig.set("weapons.bow.enchantmentchance", 0.1);
			itemConfig.set("weapons.bow.spawnchance", 0.4);
			save = true;
		}

		if (!itemConfig.contains("armors")) {
			itemConfig.set("armors.gold_chestplate.enchantmentchance", 0.3);
			itemConfig.set("armors.gold_chestplate.spawnchance", 0.1);
			itemConfig.set("armors.gold_leggings.enchantmentchance", 0.3);
			itemConfig.set("armors.gold_leggings.spawnchance", 0.1);
			itemConfig.set("armors.iron_chestplate.enchantmentchance", 0.1);
			itemConfig.set("armors.iron_chestplate.spawnchance", 0.1);
			itemConfig.set("armors.iron_boots.enchantmentchance", 0.1);
			itemConfig.set("armors.iron_boots.spawnchance", 0.1);
			itemConfig.set("armors.leather_leggings.enchantmentchance", 0.2);
			itemConfig.set("armors.leather_leggings.spawnchance", 0.3);
			itemConfig.set("armors.leather_boots.enchantmentchance", 0.2);
			itemConfig.set("armors.leather_boots.spawnchance", 0.3);
			itemConfig.set("armors.chainmail_chestplate.enchantmentchance", 0.1);
			itemConfig.set("armors.chainmail_chestplate.spawnchance", 0.2);
			itemConfig.set("armors.chainmail_leggings.enchantmentchance", 0.1);
			itemConfig.set("armors.chainmail_leggings.spawnchance", 0.2);
			itemConfig.set("armors.diamond_helmet.enchantmentchance", 0.01);
			itemConfig.set("armors.diamond_helmet.spawnchance", 0.1);
			itemConfig.set("armors.diamond_chestplate.enchantmentchance", 0.01);
			itemConfig.set("armors.diamond_chestplate.spawnchance", 0.1);
			itemConfig.set("armors.leather_chestplate.enchantmentchance", 0.3);
			itemConfig.set("armors.leather_chestplate.spawnchance", 0.3);
			itemConfig.set("armors.iron_leggings.enchantmentchance", 0.2);
			itemConfig.set("armors.iron_leggings.spawnchance", 0.2);
			save = true;
		}

		if (!itemConfig.contains("enchantments.swords")) {
			itemConfig.set("enchantments.swords.fire_aspect", 0.3);
			itemConfig.set("enchantments.swords.damage_all", 0.3);
			itemConfig.set("enchantments.swords.knockback", 0.3);
			itemConfig.set("enchantments.swords.damage_arthropods", 0.3);
			itemConfig.set("enchantments.swords.damage_undead", 0.3);
			save = true;
		}

		if (!itemConfig.contains("enchantments.bow")) {
			itemConfig.set("enchantments.bow.arrow_damage", 0.3);
			itemConfig.set("enchantments.bow.arrow_fire", 0.3);
			itemConfig.set("enchantments.bow.arrow_infinite", 0.3);
			itemConfig.set("enchantments.bow.arrow_knockback", 0.3);
			save = true;
		}

		if (!itemConfig.contains("enchantments.armors")) {
			itemConfig.set("enchantments.armors.protection_fall", 0.2);
			itemConfig.set("enchantments.armors.protection_projectile", 0.2);
			itemConfig.set("enchantments.armors.protection_fire", 0.2);
			save = true;
		}

		if (!itemConfig.contains("food")) {
			itemConfig.set("food.mushroom_soup", 0.2);
			itemConfig.set("food.bread", 0.4);
			itemConfig.set("food.grilled_pork", 0.5);
			itemConfig.set("food.cooked_fish", 0.5);
			itemConfig.set("food.cookie", 0.4);
			itemConfig.set("food.cooked_chicken", 0.4);
			itemConfig.set("food.rotten_flesh", 0.6);
			itemConfig.set("food.apple", 0.8);
			itemConfig.set("food.cooked_beef", 0.5);
			save = true;
		}

		if (!itemConfig.contains("items")) {
			itemConfig.set("items.map", 0.1);
			itemConfig.set("items.flint_and_steel", 0.2);
			itemConfig.set("items.arrow", 0.4);
			save = true;
		}

		if (save) {
			try {
				itemConfig.save((String) worlds.get(worldname)[0]);
			} catch (IOException e) {
				logger.severe("Could not save the itemlist!");
			}
		}
	}
}

/**
 * Below this line is not my work
 */

class RandomCollection<E> {
	private final NavigableMap<Double, E> map = new TreeMap<>();
	private final Random random;
	private double total = 0;

	public RandomCollection() {
		this(new Random());
	}

	public RandomCollection(Random random) {
		this.random = random;
	}

	public void add(double weight, E result) {
		if (weight <= 0) return;
		total += weight;
		map.put(total, result);
	}

	public E next() {
		double value = random.nextDouble() * total;
		return map.ceilingEntry(value).getValue();
	}
}
