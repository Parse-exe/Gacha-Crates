package com.gmail.cparse2021.gachacrates.menu.menus;

import com.gmail.cparse2021.gachacrates.GachaCrates;
import com.gmail.cparse2021.gachacrates.lang.Lang;
import com.gmail.cparse2021.gachacrates.menu.Menu;
import com.gmail.cparse2021.gachacrates.menu.MenuManager;
import com.gmail.cparse2021.gachacrates.struct.GachaPlayer;
import com.gmail.cparse2021.gachacrates.struct.crate.Crate;
import com.gmail.cparse2021.gachacrates.struct.crate.CrateSession;
import com.gmail.cparse2021.gachacrates.util.ItemBuilder;
import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CrateMenu extends Menu {
    private final GachaCrates plugin;
    private final HashMap<UUID, ItemStack> offhandSnapshotMap = new HashMap<>();
    private String title = "Crate Menu";
    private ItemStack backgroundItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName("&7").build();
    private ItemStack pullItem = new ItemBuilder(Material.NETHER_STAR).setDisplayName("&e&lPull").build();
    private ItemStack rewardsItem = new ItemBuilder(Material.AMETHYST_CLUSTER).setDisplayName("&e&lRewards").build();

    public CrateMenu(GachaCrates plugin) {
        super("crate");
        this.plugin = plugin;
    }

    @Override
    public void load(ConfigurationSection configurationSection) {
        if (configurationSection == null) {
            return;
        }

        title = Utils.formatString(configurationSection.getString("Title"));
        backgroundItem = Utils.decodeItem(configurationSection.getString("Background-Item",
                "WHITE_STAINED_GLASS_PANE name:&7"));
        pullItem = Utils.decodeItem(configurationSection.getString("Pull-Menu-Item",
                "NETHER_STAR name:&e&lPull lore:&f&l%pull-count%_&7pulls_remaining|&7Click_to_&f&lpull"));
        rewardsItem = Utils.decodeItem(configurationSection.getString("Rewards-Menu-Item",
                "AMETHYST_CLUSTER name:&e&lRewards lore:&7Click_to_view_&frewards_&7and_your_&fpity_tracker"));
    }

    @Override
    public void open(Player player) {
        CrateSession crateSession = plugin.getSessionManager().getCrateSession(player.getUniqueId());

        if (crateSession == null) {
            Lang.ERR_UNKNOWN.send(player);
            return;
        }
        Crate crate = crateSession.getCrate();
        GachaPlayer gachaPlayer = plugin.getPlayerCache().getPlayer(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, 27, title.replace("%crate%", crate.getName()));
        HashMap<String, String> variables = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, backgroundItem);
        }

        variables.put("%pull-count%", Integer.toString(gachaPlayer.getAvailablePulls(crate)));
        inventory.setItem(11, new ItemBuilder(pullItem.clone()).setVariables(variables).build());
        inventory.setItem(15, rewardsItem);
        offhandSnapshotMap.put(player.getUniqueId(), player.getInventory().getItemInOffHand());
        player.openInventory(inventory);
        plugin.getMenuManager().setActiveMenu(player.getUniqueId(), this);
    }

    @Override
    public void processClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        MenuManager menuManager = plugin.getMenuManager();
        CrateSession crateSession = plugin.getSessionManager().getCrateSession(player.getUniqueId());
        Optional<Menu> pullMenu = menuManager.getMenu("pull");
        Optional<Menu> rewardsMenu = menuManager.getMenu("rewards");

        if (crateSession == null) {
            player.closeInventory();
            Lang.ERR_UNKNOWN.send(player);
            return;
        }

        if (menuManager.isOnCooldown(player.getUniqueId())) {
            return;
        } else {
            menuManager.addCooldown(player.getUniqueId());
        }

        if (e.getSlot() == 11) {
            if (pullMenu.isEmpty()) {
                player.closeInventory();
                return;
            }

            pullMenu.get().open(player);
            return;
        }

        if (e.getSlot() == 15) {
            if (rewardsMenu.isEmpty()) {
                player.closeInventory();
                return;
            }

            rewardsMenu.get().open(player);
        }
    }

    @Override
    public void processClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (offhandSnapshotMap.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
        }

        plugin.getMenuManager().clearActiveMenu(player.getUniqueId());
    }
}