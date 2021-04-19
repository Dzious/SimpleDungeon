package fr.dzious.bukkit.simpledungeon.plugin;

import java.io.Console;
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
import fr.dzious.bukkit.simpledungeon.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class Dungeon {
    
    private boolean isRunning = false;
    private String name;
    private World world;
    private int duration = 0;
    private int dungeonRange = 0;

    private Location dungeonLocation;
    private Map <String, String> titles = new HashMap<>();
    private List<String> resetCommands = new ArrayList<>();
    private List<String> errorCommands = new ArrayList<>();
    
    private Map<Integer, Gate> gates = new HashMap<>();
    private Map<Integer, Room> rooms = new HashMap<>();

    private int dungeonTaskId = -1;

    public Dungeon(String dungeonName) {
        name = dungeonName;
        reload();
    }

    public void start() {
        if (!isRunning  && dungeonTaskId == -1) {
            dungeonTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleDungeon.getInstance(), new Runnable() {
                public void run() {
                    reset();
                }
            }, (long)(duration * 20));

            if (dungeonTaskId == -1) {
                Logger.instance.warning("Dungeon " + name + " failed to start");
            } else {
                isRunning = true;
                Logger.instance.info("Dungeon " + name + " started.");
            }
            gates.get(1).open();
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getLocation().distance(dungeonLocation) <= dungeonRange) {
                    p.sendTitle(titles.get("running_error_title"), titles.get("running_error_subtitle"), 5, 100, 5);
                }
            }
            for (String command : errorCommands) {
                boolean result = Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, dungeonLocation));
                Logger.instance.debugConsole("Result of command : " + command + " is : " + result);
            }
        }
    }

    public void reset() {
        if (isRunning == false) {
            Logger.instance.info("Dungeon " + name + " is not running.");
            return;
        }
        isRunning = false;
        Logger.instance.info("Dungeon " + name + " stopped.");
        if (Bukkit.getScheduler().isQueued(dungeonTaskId))
            Bukkit.getScheduler().cancelTask(dungeonTaskId);
        for (Map.Entry<Integer, Room> room : rooms.entrySet()) {
            room.getValue().reset(world, dungeonLocation, titles.get("reset_title"), titles.get("reset_subtitle"));
        }
        for (Map.Entry<Integer, Gate> gate : gates.entrySet()) {
            gate.getValue().close(false);
        }
        dungeonTaskId = -1;
        for (String command : resetCommands) {
            Logger.instance.debugConsole("Command : " + Utils.formatCommand(command, dungeonLocation));
            Logger.instance.debugConsole("Result of command : " + command + " is : " + Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, dungeonLocation)));
        }
    }

    public void resetRoom(int roomId) {
        if (roomId > 0 && rooms.get(roomId) != null) {
            rooms.get(roomId).runCommands();
            Logger.instance.debugConsole("Commands has been exectuted for room " + roomId);
        }
        Logger.instance.debugConsole("Room " + roomId + " does not exists");
    }

    public static boolean isWellFormated(String dungeonName) {
        
        try {
            YamlConfiguration dungeonFile = new YamlConfiguration();

            dungeonFile.load(SimpleDungeon.getInstance().getDataFolder().getPath() + "/dungeons/" + dungeonName + ".yml");

            if (!dungeonFile.contains("world")  ||
                !dungeonFile.contains("duration") ||
                !dungeonFile.contains("dungeon.location.x") ||
                !dungeonFile.contains("dungeon.location.y") ||
                !dungeonFile.contains("dungeon.location.z") ||
                !dungeonFile.contains("dungeon.location.yaw") ||
                !dungeonFile.contains("dungeon.location.pitch") ||
                !dungeonFile.contains("dungeon.range") ||
                !dungeonFile.contains("dungeon.running_error.title") ||
                !dungeonFile.contains("dungeon.running_error.subtitle") ||
                !dungeonFile.contains("dungeon.running_error.commands") ||
                !dungeonFile.contains("dungeon.reset.title") ||
                !dungeonFile.contains("dungeon.reset.subtitle") ||
                !dungeonFile.contains("dungeon.reset.commands")) {
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
            Logger.instance.exception(e);
            return (false);
        }
        return (true);
    }

    public Gate getGate(int gateId) {
        return (gates.get(gateId));
    }

    public Room getRoom(int roomId) {
        return (rooms.get(roomId));
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
            Logger.instance.info("Dungeon " + name + " is loading.");

            YamlConfiguration dungeonFile = new YamlConfiguration();
            dungeonFile.load(SimpleDungeon.getInstance().getDataFolder().getPath() + "/dungeons/" + name + ".yml");

            world = SimpleDungeon.getInstance().getServer().getWorld(dungeonFile.getString("world"));

            if (world == null) {
                Logger.instance.error("World " + dungeonFile.getString("world") + " does not exists");
                throw new NullPointerException();
            }

            duration = dungeonFile.getInt("duration");
            
            dungeonLocation = new Location(world,
                dungeonFile.getInt("dungeon.location.x"),
                dungeonFile.getInt("dungeon.location.y"),
                dungeonFile.getInt("dungeon.location.z"),
                dungeonFile.getInt("dungeon.location.yaw"),
                dungeonFile.getInt("dungeon.location.pitch"));

            dungeonRange = dungeonFile.getInt("dungeon.range");

            if (titles.containsKey("reset_title")) {
                titles.replace("reset_title", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.reset.title")));
            } else {
                titles.put("reset_title", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.reset.title")));
            }

            if (titles.containsKey("reset_subtitle")) {
                titles.replace("reset_subtitle", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.reset.subtitle")));
            } else {
                titles.put("reset_subtitle", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.reset.subtitle")));
            }

            if (titles.containsKey("running_error_title")) {
                titles.replace("running_error_title", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.running_error.title")));
            } else {
                titles.put("running_error_title", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.running_error.title")));
            }

            if (titles.containsKey("running_error_subtitle")) {
                titles.replace("running_error_subtitle", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.running_error.subtitle")));
            } else {
                titles.put("running_error_subtitle", ChatColor.translateAlternateColorCodes('&', dungeonFile.getString("dungeon.running_error.subtitle")));
            }

            List<?> yamlList = dungeonFile.getList("dungeon.running_error.commands");

            errorCommands.clear();
            for (int i = 0; yamlList != null &&  i < yamlList.size(); i++) {
                if (yamlList.get(i) instanceof String) {
                    Logger.instance.debugConsole("Command " + (String)yamlList.get(i) + " added upon running error.");
                    errorCommands.add((String)yamlList.get(i));
                } else {
                    Logger.instance.error(yamlList.get(i).toString() + " is not a valid command.");
                }
            }

            yamlList = dungeonFile.getList("dungeon.reset.commands");

            resetCommands.clear();
            for (int i = 0; yamlList != null &&  i < yamlList.size(); i++) {
                if (yamlList.get(i) instanceof String) {
                    Logger.instance.debugConsole("Command " + (String)yamlList.get(i) + " added upon reset.");
                    resetCommands.add((String)yamlList.get(i));
                } else {
                    Logger.instance.error(yamlList.get(i).toString() + " is not a valid command.");
                }
            }

            for (int id = 1; dungeonFile.contains("gate_" + id); id++) {
                if (gates.get(id) == null)
                    gates.put(id, new Gate(dungeonFile, id));    
                else
                    gates.get(id).reload(dungeonFile, id);
            }

            for (int id = 1; dungeonFile.contains("room_" + id); id++) {
                if (rooms.get(id) == null)
                    rooms.put(id, new Room(dungeonFile, id));
                else
                    rooms.get(id).reload(dungeonFile, id);
            }

            Logger.instance.info(name + " dungeon has been loaded properly");
        } catch (Exception e) {
            Logger.instance.debugConsole("Exception");
            Logger.instance.warning("Dungeon " + name + " file could not be loaded.");
            Logger.instance.exception(e);
        }
    }

    public boolean isRunning() {
        return (isRunning);
    }


}
