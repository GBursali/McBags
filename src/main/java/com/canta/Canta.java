package com.canta;

import com.canta.events.CantaListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Canta extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        new CantaListener(this);
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
