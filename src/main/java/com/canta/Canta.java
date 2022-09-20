package com.canta;

import com.canta.events.CantaListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Canta extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        new CantaListener(this);
        this.saveDefaultConfig();
    }
    public String getConfigString(String path){
        return Objects.requireNonNull(this.getConfig().getString(path)).replace('&', ChatColor.COLOR_CHAR);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
