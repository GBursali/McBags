package com.canta.events;

import com.canta.Canta;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;

public class CantaListener implements Listener {
    private final Canta callerPlugin;
    public CantaListener(Canta plugin) {
        callerPlugin=plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    public ConfigurationSection getBagTypes() {
        return callerPlugin.getConfig().getConfigurationSection("items");
//        if(fileStream==null)
//            throw new RuntimeException("items.yml file not found");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
//        YamlConfiguration config = new YamlConfiguration();
//        try {
//            config.load(reader);
//        } catch (IOException | InvalidConfigurationException e) {
//            throw new RuntimeException(e);
//        }
//        return config;
    }
    private boolean isItemIsABag(ItemStack item){
        if(!item.hasItemMeta())
            return false;
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        ConfigurationSection items = getBagTypes();
        if(items.get("items."+meta.getDisplayName()) != null)
            return false;
        String material = items.getString(String.format("%s.material", meta.getDisplayName()));
        return item.getType().equals(Material.getMaterial(Objects.requireNonNull(material)));
    }

    private boolean doesUserHaveBag(Player player){
        return getUserBagIndex(player) >=0;
    }

    private int getUserBagIndex(Player player){
//        if(player.isOp())
//            return 0;
        Inventory playerInventory = player.getInventory();
        for (ItemStack item : playerInventory.getContents()) {
            if(item == null || !isItemIsABag(item))
                continue;
            return playerInventory.first(item);
        }
        return -1;
    }

    private int getSlotCountOfTheBag(Player player){
        if(!doesUserHaveBag(player))
            return callerPlugin.getConfig().getInt("defaultBagSlots",18);
        ItemStack bag = Objects.requireNonNull(player.getInventory().getItem(getUserBagIndex(player)));
        return getBagTypes().getInt(Objects.requireNonNull(bag.getItemMeta()).getDisplayName() + ".slot");
    }

    private boolean checkInventory(Player inventoryOwner, @Nullable ItemStack cursor, int slot){
        int availableIndexes = getSlotCountOfTheBag(inventoryOwner);
        ItemStack currentItem = inventoryOwner.getInventory().getItem(slot);
        if(!(cursor == null || cursor.getType().equals(Material.AIR)) &&
                (currentItem == null || currentItem.getType().equals(Material.AIR)) &&
                slot>=availableIndexes){
            inventoryOwner.sendMessage(Objects.requireNonNull(callerPlugin.getConfig().getString("warnings.lockedSlot")).replace('&',ChatColor.COLOR_CHAR));
            Bukkit.getScheduler().runTask(callerPlugin, () -> {
                inventoryOwner.getInventory().addItem(cursor);
//                    inventoryOwner.setItemOnCursor(null);
                inventoryOwner.closeInventory();
            });
            return false;
        }
        return true;
    }
    @EventHandler
    public void onPlayerMoveItem(InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player inventoryOwner))
            return;
        if(!checkInventory(inventoryOwner, event.getCursor(), event.getSlot())){
            event.setResult(Event.Result.DENY);
        }
    }
}
