package com.canta.events;

import com.canta.Canta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class LockedItem {
    public ItemStack item;
    public Canta callerPlugin;
    public Material material;
    public String name;

    public LockedItem(Canta canta){
        callerPlugin = canta;
        name = "Kilitli";
        material = Material.getMaterial(callerPlugin.getConfig().getString("lockedFrameMaterial","BLACK_STAINED_GLASS_PANE"));
        assert material != null;
        item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(name);
        item.setItemMeta(meta);
    }

    public boolean isClickedToFrame(InventoryClickEvent event){
        return isFrame(event.getCurrentItem());
    }

    private boolean isFrame(ItemStack item){
        return
                item != null &&
                        item.getItemMeta() != null &&
                        item.getItemMeta().getDisplayName().equals(name) &&
                        item.getType().equals(material) &&
                        item.isSimilar(this.item);
    }

    public void lockInventory(Player player,int slots){
        for (int i = 0; i < slots; i++) {
            if(isFrame(player.getInventory().getItem(i))){
                player.getInventory().setItem(i,null);
            }
        }
        for (int i = player.getInventory().getStorageContents().length-1; i >= slots; i--) {
            if(player.getInventory().getItem(i) == null)
                player.getInventory().setItem(i,item);
        }
    }

}
