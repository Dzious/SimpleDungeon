package fr.dzious.bukkit.simpledungeon.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Logger;
import fr.dzious.bukkit.simpledungeon.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class Gate {
    List<Location> locations = new ArrayList<>();
    Map<String, String> titles = new HashMap<>();
    Map<String, Material> materials = new HashMap<>();
    List<String> commands = new ArrayList<>();
    int duration;
    int around;
    int resetRoomId = 0;

    Gate(YamlConfiguration dungeonFile, int id) {
        reload(dungeonFile, id);
    }

    public void open() {
        Logger.instance.debugConsole("Open");

        int minX = (int) Math.min(locations.get(0).getX(), locations.get(1).getX());
        int minY = (int) Math.min(locations.get(0).getY(), locations.get(1).getY());
        int minZ = (int) Math.min(locations.get(0).getZ(), locations.get(1).getZ());

        int maxX = (int) Math.max(locations.get(0).getX(), locations.get(1).getX());
        int maxY = (int) Math.max(locations.get(0).getY(), locations.get(1).getY());
        int maxZ = (int) Math.max(locations.get(0).getZ(), locations.get(1).getZ());

        World world = locations.get(0).getWorld();
        Block block;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    block = world.getBlockAt(x, y, z);                    
                    if (!block.getChunk().isLoaded() && !block.getChunk().load())
                        continue;
                    block.setType(materials.get("open"));
                    Logger.instance.debugConsole("Block at X=" + x + " Y=" + y + " Z=" + z + " has been set to " + materials.get("open"));
                    Logger.instance.debugPlayer("Block at X=" + x + " Y=" + y + " Z=" + z + " has been set to " + materials.get("open"));
                }
            }
        }

        // BlockIterator it = new BlockIterator(locations.get(0).getWorld(), locations.get(0).toVector(), locations.get(0).subtract(locations.get(1)).toVector(), 0, 0);

        // while (it.hasNext()) {
        //     Block block = it.next();
        //     if (!block.getChunk().isLoaded() && !block.getChunk().load())
        //         continue;
        //     block.setType(materials.get("open"));
        // }


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(locations.get(0)) <= around) {
                p.sendTitle(titles.get("title"),titles.get("subtitle"), 5, 100, 5);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleDungeon.getInstance(), new Runnable() {
            public void run() {
                close(true);
            }
        }, (long)(duration * 20));
    }

    public void close(boolean runCommand) {
        Logger.instance.debugConsole("Close");

        int minX = (int) Math.min(locations.get(0).getX(), locations.get(1).getX());
        int minY = (int) Math.min(locations.get(0).getY(), locations.get(1).getY());
        int minZ = (int) Math.min(locations.get(0).getZ(), locations.get(1).getZ());

        int maxX = (int) Math.max(locations.get(0).getX(), locations.get(1).getX());
        int maxY = (int) Math.max(locations.get(0).getY(), locations.get(1).getY());
        int maxZ = (int) Math.max(locations.get(0).getZ(), locations.get(1).getZ());

        Logger.instance.debugConsole("Min : X=" + minX + " Y=" + minY + " Z=" + minZ);
        Logger.instance.debugConsole("Max : X=" + maxX + " Y=" + maxY + " Z=" + maxZ);

        World world = locations.get(0).getWorld();
        Block block;

        boolean forceLoaded = false;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    block = world.getBlockAt(x, y, z);                    
                    forceLoaded = false;
                    if (!block.getChunk().isLoaded() && !(forceLoaded = block.getChunk().load()))
                        continue;
                    block.setType(materials.get("close"));
                    Logger.instance.debugConsole("Block at X=" + x + " Y=" + y + " Z=" + z + " has been set to " + materials.get("close"));
                    Logger.instance.debugPlayer("Block at X=" + x + " Y=" + y + " Z=" + z + " has been set to " + materials.get("close"));
                    if (forceLoaded == true)
                        block.getChunk().unload();
                }
            }
        }

        // BlockIterator it = new BlockIterator(locations.get(0).getWorld(), locations.get(0).toVector(), locations.get(0).subtract(locations.get(1)).toVector(), 0, 0);

        // while (it.hasNext()) {
        //     Block block = it.next();
        //     forceLoaded = false;
        //     if (!block.getChunk().isLoaded())
        //         forceLoaded = block.getChunk().load();
        //         if (forceLoaded == false)
        //             continue;
        //     block.setType(materials.get("close"));
        //     if (forceLoaded == true)
        //         block.getChunk().unload();
        // }
        // for (String command : resetCommands) {
        //     Logger.instance.debugConsole("Command : " + Utils.formatCommand(command, dungeonLocation));
        //     Logger.instance.debugConsole("Result of command : " + command + " is : " + Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, dungeonLocation)));
        // }
        if (runCommand == true) {
            for (String command : commands) {
                boolean rtn = Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, locations.get(0)));
                Logger.instance.debugConsole("Command " + command + " return value is " + rtn);
            }
        }
    }

    public int getDuration() {
        return (duration);
    }

    public int getResetRoomId() {
        return (resetRoomId);
    }

    public void reload(YamlConfiguration dungeonFile, int id) {
        Logger.instance.info("Gate " + id + " is loading.");

        locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
            dungeonFile.getString("world")),
            dungeonFile.getInt("gate_" + id + ".start.x"),
            dungeonFile.getInt("gate_" + id + ".start.y"),
            dungeonFile.getInt("gate_" + id + ".start.z")));
            
        locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
            dungeonFile.getString("world")),
            dungeonFile.getInt("gate_" + id + ".end.x"),
            dungeonFile.getInt("gate_" + id + ".end.y"),
            dungeonFile.getInt("gate_" + id + ".end.z")));
    
        materials.put("open", Material.valueOf(dungeonFile.getString("gate_" + id + ".material.open").toUpperCase()));
        materials.put("close", Material.valueOf(dungeonFile.getString("gate_" + id + ".material.close").toUpperCase()));
    
        titles.put("title", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("gate_" + id + ".title")));
        titles.put("subtitle", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("gate_" + id + ".subtitle")));
    
        duration = dungeonFile.getInt("gate_" + id + ".duration");
    
        List<?> yamlList = dungeonFile.getList("gate_" + id + ".commands");
        Logger.instance.debugConsole(" gate_" + id + ".commands list  : " + yamlList);

        for (int i = 0; yamlList != null &&  i < yamlList.size(); i++) {
            if (yamlList.get(i) instanceof String) {
                commands.add((String)yamlList.get(i));
                Logger.instance.debugConsole("Command " + (String)yamlList.get(i) + " added upon gate close.");
            } else {
                Logger.instance.warning(yamlList.get(i).toString() + " is not considered as string.");
            }
        }
        around = dungeonFile.getInt("gate_" + id + ".around");
        if (dungeonFile.contains("gate_"+ id + ".reset_room")) {
            resetRoomId = dungeonFile.getInt("gate_"+ id + ".reset_room");
            Logger.instance.info("Gate " + id + " will reset room " + resetRoomId);
        }

        Logger.instance.info("Gate " + id + " loaded properly.");
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
            !dungeonFile.contains("gate_"+ id +".commands") &&
            !dungeonFile.contains("gate_"+ id +".around")) {
                return (false);
        }
        return (true);
    }
}
