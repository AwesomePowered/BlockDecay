package net.poweredbyawesome.blockdecay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BlockDecay extends JavaPlugin implements Listener {

    int defaultDecay;
    private Map<Location, Long> blocks = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        defaultDecay = getConfig().getInt("default.time", 5);
        loadBlocks();
        poll();
    }

    @Override
    public void onDisable() {
        if (!blocks.isEmpty()) {
            File file = new File(this.getDataFolder(), "blocks.yml");
            FileConfiguration storage = new YamlConfiguration();
            try {
                storage.load(file);
                storage.set("blocks", null);
                for (Location loc : blocks.keySet()) {
                    storage.set("blocks." + LocationUtils.locToString(loc), blocks.get(loc));
                }
                storage.save(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadBlocks() {
        ConfigurationSection section = getBlockStorage().getConfigurationSection("blocks");
        if (section == null) {
            return;
        }
        for (String s : section.getKeys(false)) {
            blocks.put(LocationUtils.stringToLoc(s), getBlockStorage().getLong("blocks."+s));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent ev) {
        String mat = ev.getBlock().getType().toString();
        if (!getConfig().getStringList("worlds").contains(ev.getBlock().getWorld().getName())) {
            return;
        }
        if (!(getConfig().getStringList("whitelist").contains(mat) || getConfig().getConfigurationSection("decay").getKeys(false).contains(mat))) {
            return;
        }

        if (!ev.getPlayer().hasPermission("blockdecay.bypass")) {
            int decayTime = getConfig().getInt("decay."+mat+".time", defaultDecay);
            if (decayTime < 300) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        ev.getBlock().setType(Material.valueOf(getConfig().getString("default.material")));
                    }
                }, decayTime * 20);
            } else {
                blocks.put(ev.getBlock().getLocation(), getEpoch() + decayTime);
            }
        }
    }

    private void poll() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Iterator<Map.Entry<Location, Long>> i = blocks.entrySet().iterator(); i.hasNext();) {
                    Map.Entry<Location, Long> e = i.next();
                    if (e.getValue() <= getEpoch()) {
                        e.getKey().getBlock().setType(Material.valueOf(getConfig().getString("default.material")));
                        i.remove();
                    }
                }
            }
        }.runTaskTimer(this, 100, 20);
    }

    private FileConfiguration getBlockStorage() {
        File file = new File(this.getDataFolder(), "blocks.yml");
        FileConfiguration storage = new YamlConfiguration();
        try {
            if (!file.exists()) {
                saveResource("blocks.yml", false);
            }
            storage.load(file);
            return storage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private long getEpoch() {
        return System.currentTimeMillis() / 1000;
    }
}
