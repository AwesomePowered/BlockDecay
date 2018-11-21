package net.poweredbyawesome.blockdecay;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    public static String locToString(Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return world+"~"+x+"~"+y+"~"+z;
    }

    public static Location stringToLoc(String s) {
        String[] loc = s.split("~");
        return new Location(Bukkit.getWorld(loc[0]),Double.valueOf(loc[1]),Double.valueOf(loc[2]),Double.valueOf(loc[3]));
    }
}
