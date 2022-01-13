package com.gmail.cparse2021.gachacrates.cache;

import org.bukkit.configuration.file.FileConfiguration;

public class GachaConfig {
    public static int MAX_PULLS = 20;

    public static void load(FileConfiguration fileConfiguration) {
        GachaConfig.MAX_PULLS = fileConfiguration.getInt("Max-Pulls", 20);
    }
}