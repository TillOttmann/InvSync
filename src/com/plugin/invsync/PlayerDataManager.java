package com.plugin.invsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

class PlayerDataManager {
	private Connection conn;
	
	PlayerDataManager() {
		conn = InvSyncMain.getDatabaseConnection();
	}
	
	// Holt einen beliebigen Wert aus der Datenbank
	// Diese generische Methode funktioniert, da die Tabellen alle dasselbe Schema
	// (UUID + DATA) haben
	// Die Methode gibt ein Objekt wieder, welches in den einzelnen Methoden getypecasted wird
	private Object getValueFromDB(String table, String column, String uuid) {
		try {
			PreparedStatement preparedStmt;
			
			// Da diese Werte kein User-Input sind sondern als konstante Parameter weitergegeben werden,
			// müssen sie nicht als Teil des PreparedStatement angegeben werden
			String getValue = "SELECT " + column + " FROM " + table + " WHERE uuid = ?";
			
			preparedStmt = conn.prepareStatement(getValue);
			preparedStmt.setString(1, uuid);
			
			ResultSet result = preparedStmt.executeQuery();
			
			if (result.next()) {
				return result.getObject(column);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Weitere generische Methode
	// REPLACE INTO ist ähnlich INSERT INTO aber falls bereits eine Zeile
	// mit demselben PRIMARY KEY vorhanden ist, wird diese überschrieben
	private void replaceValueIntoDB(String table, String column, Object value, String uuid) {
		try {
			PreparedStatement preparedStmt;
			
			String insertHealth = "INSERT INTO " + table + " (uuid, " + column + ")"
					+ " VALUES (?, ?) ON DUPLICATE KEY UPDATE " + column + "=?";
			
			preparedStmt = conn.prepareStatement(insertHealth);
			preparedStmt.setString(1, uuid);
			preparedStmt.setObject(2, value);
			preparedStmt.setObject(3, value);
			
			preparedStmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void loadSaturation(Player player) {
		Object saturation = getValueFromDB("playerSaturationValues", "saturation", uuid(player));
		if (saturation instanceof Float && saturation != null) player.setSaturation((Float) saturation);
	}
	
	void saveSaturation(Player player) {
		replaceValueIntoDB("playerSaturationValues", "saturation", player.getSaturation(), uuid(player));
	}
	
	void loadFoodLevel(Player player) {
		Object foodLevel = getValueFromDB("playerFoodLevels", "foodlevel", uuid(player));
		if (foodLevel instanceof Integer && foodLevel != null) player.setFoodLevel((Integer) foodLevel);
	}
	
	void saveFoodLevel(Player player) {
		replaceValueIntoDB("playerFoodLevels", "foodlevel", player.getFoodLevel(), uuid(player));
	}
	void loadCurrentSlot(Player player) {
		Object currentSlot = getValueFromDB("playerCurrentSlots", "currentslot", uuid(player));
		if (currentSlot instanceof Integer && currentSlot != null) {
			if ((Integer) currentSlot >= 0 && (Integer) currentSlot <= 8) {
				player.getInventory().setHeldItemSlot((Integer) currentSlot);
			}
		}
	}
	
	void saveCurrentSlot(Player player) {
		replaceValueIntoDB("playerCurrentSlots", "currentslot", player.getInventory().getHeldItemSlot(), uuid(player));
	}
	// Holt sich die Inhalte der Enderchest aus der DB und decodiert sie
	void loadEC(Player player) {
		String ECString = "";
		Object EC = getValueFromDB("playerEnderChests", "enderchest",uuid(player));
		if (EC instanceof String && EC != null) {
			ECString = (String) EC;
		}
		
		if (!ECString.isEmpty()) {
			String decodedECString = decodeFromB64(ECString);
			ArrayList<ItemStack> ECAsArrayList = new ArrayList<>();
			String[] itemsAsStringArray = decodedECString.split("\\|");
			
			for (String item : itemsAsStringArray) {
				ECAsArrayList.add(stringBlobToItem(item));
			}
			player.getEnderChest().setContents(ECAsArrayList.toArray(new ItemStack[0]));
		}
	}
	
	// Speichert die Enderchest in Base64 in der DB
	void saveEC(Player player) {
		Inventory EC = player.getEnderChest();
		
		ItemStack[] items = EC.getContents();
		String ECString = "";
		
		for (ItemStack item : items) {
			ECString +=  itemToStringBlob(item) + "|";
		}
		
		String encodedECString = encodeToB64(ECString);
		replaceValueIntoDB("playerEnderChests", "enderchest", encodedECString, uuid(player));
	}
	
	// Läd die Effekte und ihre Dauer, etc aus der DB und decodiert sie
	// Die Effekte werden mithilfe von .serialize() in folgendes Format gebracht:
	// {effect=minecraft:fire_resistance, 
	// duration=3922, amplifier=0, ambient=false, has-particles=true, has-icon=true}
	// Von dort aus werden sie in der DB gespeichert
	// Um die Daten nun wieder in die als Parameter von dem Konstrukter von PotionEffekt gewünschten form
	// zu bringen, wird der String nach und nach durch deserialize() zerlegt und in die gewünschten Datentypen
	// geparst. Das Ergebnis wird in einer HashMap der Form Map<String, Object> zurückgegeben und als 
	// Parameter des Konstruktors verwendet. Es gibt Stand 16.10.24 keine bessere / effizientere Methode
	// dies ohne Hilfe von externen Libaries zu tun
	// Sehe https://www.spigotmc.org/threads/de-serializing-potioneffect-objects.666375/
	void loadEffects(Player player) {	
		Object effects = getValueFromDB("playerEffects", "effects", uuid(player));
		if (effects instanceof String && effects != null) {
			 Collection<PotionEffect> effectsCollection = player.getActivePotionEffects();
			 for (PotionEffect effect : effectsCollection) {
				 player.removePotionEffect(effect.getType());
			 }
			String effectsString = (String) effects;
			if (!effectsString.isEmpty()) {
				String[] effectsArray = (effectsString.substring(0, effectsString.length() -1)).split("\\|");

				for (String effect : effectsArray) {
					PotionEffect potionEffect = new PotionEffect(deserialize(effect));
					player.addPotionEffect(potionEffect);
				}
			}
		}
	}
	
	// Speichert die Effekte in Base64 in der DB
	// Nähere Beschreibung: Siehe oben - loadEffects()
	void saveEffects(Player player) {
		 Collection<PotionEffect> effectsCollection = player.getActivePotionEffects();
		 String effects = "";
		 for (PotionEffect effect : effectsCollection) {
			 effects += effect.serialize() + "|";
		 }
		 replaceValueIntoDB("playerEffects", "effects", effects, uuid(player));
	}
	
	// Leben werden geladen
	void loadHealth(Player player) {
		Object health = getValueFromDB("playerHealthValues", "health", uuid(player));
		if (health instanceof Double && health != null) player.setHealth((Double) health);
	}
	
	// Leben werden gespeichert
	void saveHealth(Player player) {
		replaceValueIntoDB("playerHealthValues", "health", player.getHealth(), uuid(player));
	}
	
	// Experience-Level wird geladen
	void loadExpLevel(Player player) {
		Object level = getValueFromDB("playerLevels", "level", uuid(player));
		if (level instanceof Integer) player.setLevel((Integer) level);
	}
	
	// Experience-Level wird gespeichert
	void saveExpLevel(Player player) {
		replaceValueIntoDB("playerLevels", "level", player.getHealth(), uuid(player));
	}
	
	// Läd die advancements aus der DB und dekodiert sie
	// Ein Loop geht durch alle Advancements, checkt ob der Spieler sie laut Db hat, falls ja gibt er dem
	// Spieler das advancement (bzw setzt die Erfüllung der Kriterien auf erfüllt, dadurch passiert nichts wenn
	// das advancement auf diesem Server bereits in world/advancements/[uuid].json vorhanden ist)
	void loadAdvancements(Player player) {
		Object advancements = getValueFromDB("playerAdvancements", "advancements", uuid(player));
		if (advancements instanceof String && advancements != null) {	
			// Quelle: https://www.spigotmc.org/threads/clearing-every-advancement-a-player-has-on-the-server.473875/ (Maxx_Qc)
			Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
			String decodedAdvancements = decodeFromB64((String) advancements);
			String[] advancementsArray = decodedAdvancements.split("\\|");
			
			while (iterator.hasNext()) {
				AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
				for (String criteria : advancementsArray) {
					if (!progress.getAwardedCriteria().contains(criteria)) {
						progress.awardCriteria(criteria);
					}
				}
			}
		}
	}
	
	// Codiert die Advancements in Base64 und speichert sie in der DB
	// Falls ein Spieler ein advancement hat wird es zu dem String advancementProgress hinzugefügt,
	// getrennt durch "|"
	void saveAdvancements(Player player) {
		
		// Quelle: https://www.spigotmc.org/threads/clearing-every-advancement-a-player-has-on-the-server.473875/ (Maxx_Qc)
		Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
		String advancementProgress = "";
		while (iterator.hasNext()) {
			AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
			for (String criteria : progress.getAwardedCriteria()) {
				advancementProgress += criteria + "|";
			}
		}
		
		String encodedAdvancementProgress = encodeToB64(advancementProgress);
		replaceValueIntoDB("playerAdvancements", "advancements", encodedAdvancementProgress, uuid(player));
	}
	
	// Holt sich das Inventar aus der DB und decodiert es
	void loadInventory(Player player) {
		String inventoryString = "";
		Object inventory = getValueFromDB("playerInventories", "inventory",uuid(player));
		if (inventory instanceof String && inventory != null) {
			inventoryString = (String) inventory;
		}
		
		if (!inventoryString.isEmpty()) {
			String decodedInventoryString = decodeFromB64(inventoryString);
			ArrayList<ItemStack> inventoryAsArrayList = new ArrayList<>();
			String[] itemsAsStringArray = decodedInventoryString.split("\\|");
			
			for (String item : itemsAsStringArray) {
				inventoryAsArrayList.add(stringBlobToItem(item));
			}
			player.getInventory().setContents(inventoryAsArrayList.toArray(new ItemStack[0]));
		}
	}
	
	// Codiert das Inventar in B64 und speichert es in der DB
	void saveInventory(Player player) {
		PlayerInventory inv = player.getInventory();
		
		ItemStack[] items = inv.getContents();
		String inventoryString = "";
		
		for (ItemStack item : items) {
			inventoryString +=  itemToStringBlob(item) + "|";
		}
		
		String encodedInventoryString = encodeToB64(inventoryString);
		replaceValueIntoDB("playerInventories", "inventory", encodedInventoryString, uuid(player));
	}
	
	// Siehe loadEffects() und saveEffects()
	Map<String, Object> deserialize(String serializedString) {
		Map<String, Object> deserializedMap = new HashMap<>();
		
		// Entfernt die geschweiften Klammern "{}"
		serializedString = serializedString.replace("{", "");
		serializedString = serializedString.replace("}", "");

		// Trennt die einzelnen Key-Value pairs
		String[] pairs = serializedString.split(",");
		
		for (String pair : pairs) {
			String[] seperatedPairs = pair.split("=");
			
			String key = seperatedPairs[0].trim();
			String value = seperatedPairs[1].trim();
			
			switch (key) {
				case "effect":
					deserializedMap.put(key, value);
					break;
				case "duration":
					deserializedMap.put(key, Integer.parseInt(value));
					break;
				case "amplifier":
					deserializedMap.put(key, Integer.parseInt(value));
					break;
				default:
					deserializedMap.put(key, Boolean.parseBoolean(value));
			}
		}
		return deserializedMap;
	}
	
	// Macht den Code übersichtlicher
	private String uuid(Player player) {
		return player.getUniqueId().toString();
	}
	
	private String encodeToB64(String input) {
		return Base64.getEncoder().encodeToString(input.getBytes());
	}
	
	private String decodeFromB64(String input) {
		byte[] decodedBytes = Base64.getDecoder().decode(input);
		return new String(decodedBytes);
	}
	
	// Quelle: https://www.spigotmc.org/threads/serializing-itemstack-to-string.80233/ (by Hellgast23)
	private String itemToStringBlob(ItemStack itemStack) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("i", itemStack);
		return config.saveToString();
	}
	
	// Quelle: https://www.spigotmc.org/threads/serializing-itemstack-to-string.80233/ (by Hellgast23)
	private ItemStack stringBlobToItem(String stringBlob) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(stringBlob);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return config.getItemStack("i", null);
	}
}
