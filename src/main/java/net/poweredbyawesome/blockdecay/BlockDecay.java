package net.poweredbyawesome.blockdecay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockDecay extends JavaPlugin implements Listener {

    int defaultDecay;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        defaultDecay = getConfig().getInt("default.time", 5);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        Player p = ev.getPlayer();
        String mat = ev.getBlock().getType().toString();
        if (!getConfig().getStringList("worlds").contains(ev.getBlock().getWorld().getName())) {
            return;
        }
        if (!(getConfig().getStringList("whitelist").contains(mat) || getConfig().getConfigurationSection("decay").getKeys(false).contains(mat))) {
            return;
        }

        if (!p.hasPermission("blockdecay.bypass")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    ev.getBlock().setType(Material.valueOf(getConfig().getString("default.material")));
                }
            }, getConfig().getInt("decay."+mat+".time", defaultDecay) * 20);
        }
    }
}
