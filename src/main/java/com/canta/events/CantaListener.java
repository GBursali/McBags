package com.canta.events;

import com.canta.Canta;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Objects;

public class CantaListener implements Listener {
    private final Canta callerPlugin;
    private final LockedItem lockFrame;
    public CantaListener(Canta plugin) {
        callerPlugin=plugin;
        lockFrame= new LockedItem(plugin);
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    public ConfigurationSection getBagTypes() {
        return callerPlugin.getConfig().getConfigurationSection("items");
    }
    private boolean isItemIsABag(ItemStack item){
        if(!item.hasItemMeta())
            return false;
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        ConfigurationSection items = getBagTypes();
        String bagName = meta.getDisplayName();
        if(items.get(bagName+".slot") == null)
            return false;
        String material = items.getString(String.format("%s.material", bagName));
        return item.getType().equals(Material.getMaterial(Objects.requireNonNull(material)));
    }
    private int getSlotCountOfTheBag(Player player){
        if(player.getGameMode().equals(GameMode.CREATIVE))
            return 36;
        Inventory playerInventory = player.getInventory();
        int maxSlotSize = callerPlugin.getConfig().getInt("defaultBagSlots",9);
        for (ItemStack item : playerInventory.getContents()) {
            if(item == null || !isItemIsABag(item))
                continue;

            maxSlotSize = Math.max(maxSlotSize, getBagSlotFromBagItem(item));
        }

        if(isItemIsABag(player.getItemOnCursor()))
            maxSlotSize = Math.max(maxSlotSize, getBagSlotFromBagItem(player.getItemOnCursor()));

        return maxSlotSize;
    }

    private int getBagSlotFromBagItem(ItemStack bag){
        if(bag.getItemMeta()==null)
            return 0;
        String bagName = bag.getItemMeta().getDisplayName();
        return getBagTypes().getInt(bagName+".slot");
    }

    private boolean checkInventory(Player inventoryOwner, @Nullable ItemStack cursor, int slot, boolean refund){
        int availableIndexes = getSlotCountOfTheBag(inventoryOwner);
        ItemStack currentItem = inventoryOwner.getInventory().getItem(slot);
        if(!(cursor == null || cursor.getType().equals(Material.AIR)) &&
                (currentItem == null || currentItem.getType().equals(Material.AIR)) &&
                slot>=availableIndexes){
            inventoryOwner.sendMessage(callerPlugin.getConfigString("warnings.lockedSlot"));
            Bukkit.getScheduler().runTask(callerPlugin, () -> {
                if(refund)
                    inventoryOwner.getInventory().addItem(cursor);
                inventoryOwner.closeInventory();
            });
            return false;
        }
        return true;
    }
    @EventHandler
    public void onPlayerMoveItem(InventoryClickEvent event){
        //Clicked by a player
        if (!(event.getWhoClicked() instanceof Player inventoryOwner))
            return;
        Bukkit.getScheduler().runTask(callerPlugin,x-> lockFrame.lockInventory(inventoryOwner,getSlotCountOfTheBag(inventoryOwner)));
        //Clicking the empty sides of the inventory + clicked in the chest
        if(event.getClickedInventory()==null || !event.getClickedInventory().getType().equals(InventoryType.PLAYER))
            return;
        //Clicked item is not our LockedFrame
        if(lockFrame.isClickedToFrame(event)){
            event.setResult(Event.Result.DENY);
            inventoryOwner.sendMessage(callerPlugin.getConfigString("warnings.lockedSlotClick"));
            inventoryOwner.closeInventory();
            return;
        }
        boolean refund = event.getView().getTopInventory().getType().equals(InventoryType.CRAFTING);
        if(!checkInventory(inventoryOwner, event.getCursor(), event.getSlot(),refund)){
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        int slots = getSlotCountOfTheBag(player);
        lockFrame.lockInventory(player,slots);
    }
}
