package fr.dzious.bukkit.simpledungeon.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class Gate {
    List<Location> locations = new ArrayList<>();
    Map<String, String> titles = new HashMap<>();
    Map<String, Material> materials = new HashMap<>();
    String command;
    int duration;
    int around;

    Gate(YamlConfiguration dungeonFile, int id) {
        reload(dungeonFile, id);
    }

    public void open() {
        BlockIterator it = new BlockIterator(locations.get(0).getWorld(), locations.get(0).toVector(), locations.get(0).subtract(locations.get(1)).toVector(), 0, 0);
        
        while (it.hasNext()) {
            Block block = it.next();
            if (!block.getChunk().isLoaded() && !block.getChunk().load())
                continue;
            block.setType(materials.get("open"));
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(locations.get(0)) <= around) {
                p.sendTitle(titles.get("title"),titles.get("subtitle"), 5, 10, 5);
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleDungeon.getInstance(), new Runnable() {
            public void run() {
                close();
                // Code here...
                // This code will fire after the specified delay [below]
            }
        }, (long)(duration * 20));
    }

    public void close() {
        BlockIterator it = new BlockIterator(locations.get(0).getWorld(), locations.get(0).toVector(), locations.get(0).subtract(locations.get(1)).toVector(), 0, 0);
        boolean forceLoaded = false;

        while (it.hasNext()) {
            Block block = it.next();
            forceLoaded = false;
            if (!block.getChunk().isLoaded())
                forceLoaded = block.getChunk().load();
                if (forceLoaded == false)
                    continue;
            block.setType(materials.get("close"));
            if (forceLoaded == true)
                block.getChunk().unload();
        }
        Bukkit.dispatchCommand(SimpleDungeon.getInstance().getServer().getConsoleSender(), command);
    }

    public int getDuration() {
        return (duration);
    }

    public void reload(YamlConfiguration dungeonFile, int id) {
        locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
            dungeonFile.getString("world")),
            dungeonFile.getInt("gate_"+ id +".start.x"),
            dungeonFile.getInt("gate_"+ id +".start.y"),
            dungeonFile.getInt("gate_"+ id +".start.z")));
            
            locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
            dungeonFile.getString("world")),
            dungeonFile.getInt("gate_"+ id +".end.x"),
            dungeonFile.getInt("gate_"+ id +".end.y"),
            dungeonFile.getInt("gate_"+ id +".end.z")));
    
            materials.put("open", Material.valueOf(dungeonFile.getString("gate_"+ id +".material.open")));
            materials.put("close", Material.valueOf(dungeonFile.getString("gate_"+ id +".material.close")));
    
            titles.put("title", dungeonFile.getString("gate_"+ id +".title"));
            titles.put("title", dungeonFile.getString("gate_"+ id +".subtitle"));
    
            duration = dungeonFile.getInt("gate_"+ id +".duration");
    
            command = dungeonFile.getString("gate_"+ id +".command");
    
            around = dungeonFile.getInt("gate_"+ id +".around");
    }

    public static boolean isWellFormated(YamlConfiguration dungeonFile, int id) {
        if (!dungeonFile.contains("gate_"+ id +".start.x") &&
            !dungeonFile.contains("gate_"+ id +".start.y") &&
            !dungeonFile.contains("gate_"+ id +".start.z") &&
            !dungeonFile.contains("gate_"+ id +".end.x") &&
            !dungeonFile.contains("gate_"+ id +".end.y") &&
            !dungeonFile.contains("gate_"+ id +".end.z") &&
            !dungeonFile.contains("gate_"+ id +".material.open") &&
            !dungeonFile.contains("gate_"+ id +".material.close") &&
            !dungeonFile.contains("gate_"+ id +".title") &&
            !dungeonFile.contains("gate_"+ id +".subtitle") &&
            !dungeonFile.contains("gate_"+ id +".duration") &&
            !dungeonFile.contains("gate_"+ id +".command") &&
            !dungeonFile.contains("gate_"+ id +".around")) {
                return (false);
        }
        return (true);
    }
}
