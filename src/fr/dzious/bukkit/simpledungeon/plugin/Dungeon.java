package fr.dzious.bukkit.simpledungeon.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Logger;

public class Dungeon {
    
    boolean isRunning = false;
    String name;
    World world;
    int duration;

    Location resetLocation;
    Map <String, String> resetTitles = new HashMap<>();
    String resetCommand;
    
    Map<Integer, Gate> gates = new HashMap<>();
    Map<Integer, Room> rooms = new HashMap<>();


    public Dungeon(String dungeonName) {
        name = dungeonName;

        try {
            YamlConfiguration dungeonFile = new YamlConfiguration();
            dungeonFile.load(SimpleDungeon.getInstance().getDataFolder().getPath() + "/dungeons/" + dungeonName + ".yml");

            name = dungeonName;

            world = SimpleDungeon.getInstance().getServer().getWorld(dungeonFile.getString("world"));

            duration = dungeonFile.getInt("duration");

            resetLocation = new Location(world,
            dungeonFile.getInt("reset.location.x"),
            dungeonFile.getInt("reset.location.y"),
            dungeonFile.getInt("reset.location.z"),
            dungeonFile.getInt("reset.location.yaw"),
            dungeonFile.getInt("reset.location.pitch"));    

            resetTitles.put("title", dungeonFile.getString("reset.title"));

            resetTitles.put("subtitle", dungeonFile.getString("reset.subtitle"));
                

            resetCommand = dungeonFile.getString("reset.command");

            for (int id = 1; dungeonFile.contains("gate_" + id); id++) {
                gates.put(id, new Gate(dungeonFile, id));
            }

            for (int id = 1; dungeonFile.contains("room_" + id); id++) {
                rooms.put(id, new Room(dungeonFile, id));
            }
        } catch (Exception e) {
            Logger.instance.warning("Dungeon " + dungeonName + " file could not be loaded.");
            e.printStackTrace();
        }
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleDungeon.getInstance(), new Runnable() {
                public void run() {
                    reset();
                }
            }, (long)(duration * 20));
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().distance(resetLocation) <= 5) {
                    p.sendTitle(resetTitles.get("title"), resetTitles.get("subtitle"), 5, 10, 5);
                  }
                
            }
        }
    }

    public void reset() {
        for (Map.Entry<Integer, Room> room : rooms.entrySet()) {
            room.getValue().reset(world, resetLocation, resetTitles);
        }
        for (Map.Entry<Integer, Gate> gate : gates.entrySet()) {
            gate.getValue().close();
        }
        isRunning = false;
    }

    public static boolean isWellFormated(String dungeonName) {
        
        try {
            YamlConfiguration dungeonFile = new YamlConfiguration();

            dungeonFile.load(SimpleDungeon.getInstance().getDataFolder().getPath() + "/dungeons/" + dungeonName + ".yml");

            if (!dungeonFile.contains("world")  ||
                !dungeonFile.contains("duration") ||
                !dungeonFile.contains("reset.location.x") ||
                !dungeonFile.contains("reset.location.y") ||
                !dungeonFile.contains("reset.location.z") ||
                !dungeonFile.contains("reset.location.yaw") ||
                !dungeonFile.contains("reset.location.pitch") ||
                !dungeonFile.contains("reset.title") ||
                !dungeonFile.contains("reset.subtitle") ||
                !dungeonFile.contains("reset.command")) {
                    Logger.instance.warning("File " + dungeonName + " has a reset element missing.");
                    return (false);
            }

            int id = 0;
            for (id = 1; dungeonFile.contains("gate_" + id); id++) {
                if (!Gate.isWellFormated(dungeonFile, id)) {
                    Logger.instance.warning("Gate " + id + " of " + dungeonName + " is not formated correctly.");
                    return (false); 
                }
            }
            if (id == 1) {
                Logger.instance.warning("Dungeon " + dungeonName + " is not formated correctly.");
                return (false);
            }

            for (id = 1; dungeonFile.contains("room_" + id); id++) {
                if (!Room.isWellFormated(dungeonFile, id)) {
                    Logger.instance.warning("Gate " + id + " of " + dungeonName + " is not formated correctly.");
                    return (false);
                }
            }
            if (id == 1) {
                Logger.instance.warning("Dungeon " + dungeonName + " is not formated correctly.");
                return (false);
            }
        } catch (Exception e) {
            Logger.instance.warning("Dungeon " + dungeonName + " file could not be loaded.");
            return (false);
        }
        return (true);
    }

    public Gate getGate(int gateId) {
        return (gates.get(gateId));
    }

    public List<String> getGatesIds() {
        List<String> gatesIds = new ArrayList<>();
    
        for (Map.Entry<Integer,Gate> gate : gates.entrySet()) {
            gatesIds.add(gate.getKey().toString());
        }
        return (gatesIds);
    }

    public void reload() {
        if (!isWellFormated(name))
            return;
        try {
            YamlConfiguration dungeonFile = new YamlConfiguration();
            dungeonFile.load(SimpleDungeon.getInstance().getDataFolder().getPath() + "/" + name);

            world = SimpleDungeon.getInstance().getServer().getWorld(dungeonFile.getString("world"));

            duration = dungeonFile.getInt("duration");

            resetLocation = new Location(world,
            dungeonFile.getInt("reset.location.x"),
            dungeonFile.getInt("reset.location.y"),
            dungeonFile.getInt("reset.location.z"),
            dungeonFile.getInt("reset.location.yaw"),
            dungeonFile.getInt("reset.location.pitch"));    

            resetTitles.put("title", dungeonFile.getString("reset.title"));

            resetTitles.put("subtitle", dungeonFile.getString("reset.subtitle"));
                

            resetCommand = dungeonFile.getString("reset.command");

            for (int id = 0; dungeonFile.contains("gate_" + id); id++) {
                gates.get(id).reload(dungeonFile, id);
            }

            for (int id = 0; dungeonFile.contains("room_" + id); id++) {
                rooms.get(id).reload(dungeonFile, id);
            }
        } catch (Exception e) {
            Logger.instance.warning("Dungeon " + name + " file could not be loaded.");
        }
    }

    public boolean isRunning() {
        return (isRunning);
    }
}
