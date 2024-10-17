package com.plugin.invsync;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private PlayerDataManager manager;
    
    PlayerListener() {
    	manager = new PlayerDataManager();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
    	Player player = e.getPlayer();
    	manager.loadInventory(player);
    	manager.loadAdvancements(player);
    	manager.loadExpLevel(player);
    	manager.loadHealth(player);
    	manager.loadEffects(player);
    	manager.loadEC(player);
    	manager.loadFoodLevel(player);
    	manager.loadSaturation(player);
    	manager.loadCurrentSlot(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
    	Player player = e.getPlayer();
    	manager.saveInventory(player);
    	manager.saveAdvancements(player);
    	manager.saveExpLevel(player);
    	manager.saveHealth(player);
    	manager.saveEffects(player);
    	manager.saveEC(player);
    	manager.saveFoodLevel(player);
    	manager.saveSaturation(player);
    	manager.saveCurrentSlot(player);
    }
}
