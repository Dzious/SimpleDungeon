package fr.dzious.bukkit.simpledungeon.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Logger;
import fr.dzious.bukkit.simpledungeon.utils.Utils;

public class Room {
    private enum SHAPE {
        CIRCLE,
        SQUARE
    };

    int radius = 0;
    boolean disableLoot; 
    List<Location> locations = new ArrayList<>();
    List<String> commands = new ArrayList<>();
    SHAPE shape = SHAPE.CIRCLE;


    Room(YamlConfiguration dungeonFile, int id) {
        reload(dungeonFile, id);
    }

    private boolean isInRange(Location playerLocation) {
        if (shape == SHAPE.CIRCLE) {
            Logger.instance.debugConsole("Location : " + locations.get(0));
            Logger.instance.debugConsole("Distance : " + playerLocation.distance(locations.get(0)));
            Logger.instance.debugConsole("Radius : " + radius);
            return (playerLocation.distance(locations.get(0)) <= radius);
        } else {
            double minX = Double.min(locations.get(0).getX(), locations.get(1).getX());
            double minY = Double.min(locations.get(0).getY(), locations.get(1).getY());
            double minZ = Double.min(locations.get(0).getZ(), locations.get(1).getZ());

            double maxX = Double.max(locations.get(0).getX(), locations.get(1).getX());
            double maxY = Double.max(locations.get(0).getY(), locations.get(1).getY());
            double maxZ = Double.max(locations.get(0).getZ(), locations.get(1).getZ());

            if (minX <= playerLocation.getX() && playerLocation.getX() <= maxX &&
                minY <= playerLocation.getY() && playerLocation.getY() <= maxY &&
                minZ <= playerLocation.getZ() && playerLocation.getZ() <= maxZ)
                    return (true);
            else
                return (false);
        }
    }

    private Location getCenter() {
        if (shape == SHAPE.CIRCLE) {
            return (locations.get(0));
        } else {
            double minX = Double.min(locations.get(0).getX(), locations.get(1).getX());
            double minY = Double.min(locations.get(0).getY(), locations.get(1).getY());
            double minZ = Double.min(locations.get(0).getZ(), locations.get(1).getZ());

            double maxX = Double.max(locations.get(0).getX(), locations.get(1).getX());
            double maxY = Double.max(locations.get(0).getY(), locations.get(1).getY());
            double maxZ = Double.max(locations.get(0).getZ(), locations.get(1).getZ());

            return (new Location(locations.get(0).getWorld(), (minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2));
        }
    }

    public void runCommands() {
        for (String command : commands)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, getCenter()));
    }

    public void reset(World dungeonWorld, Location dungeonEntry, String title, String subtitle) {
        for (Player p : locations.get(0).getWorld().getPlayers()) {
            if (isInRange(p.getLocation())) {
                Logger.instance.debugConsole("Player " + p + " is in range");
            // if (p.getLocation().distance(locations.get(0)) <= radius) {
                p.sendTitle(title, subtitle, 5, 100, 5);
                p.teleport(dungeonEntry);
            } else {
                Logger.instance.debugConsole("Player " + p + " is not in range");
            }
        }
        
        boolean doMobLoot = dungeonWorld.getGameRuleValue(GameRule.DO_MOB_LOOT);
        if (disableLoot && doMobLoot)
            dungeonWorld.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        for (String command : commands)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), Utils.formatCommand(command, getCenter()));
        if (disableLoot && doMobLoot)
            dungeonWorld.setGameRule(GameRule.DO_ENTITY_DROPS, true);
    }

    public void reload(YamlConfiguration dungeonFile, int id) {
        Logger.instance.info("Room " + id + " is loading.");

        if (dungeonFile.getString("room_" + id + ".shape").equals("circle")) {
            shape = SHAPE.CIRCLE;
        } else {
            shape = SHAPE.SQUARE;
        }

        if (shape == SHAPE.CIRCLE) {
            locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
                dungeonFile.getString("world")),
                dungeonFile.getInt("room_"+ id +".circle.x"),
                dungeonFile.getInt("room_"+ id +".circle.y"),
                dungeonFile.getInt("room_"+ id +".circle.z")));
            radius = dungeonFile.getInt("room_"+ id +".circle.radius");
        } else {
            locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
                dungeonFile.getString("world")),
                dungeonFile.getInt("room_"+ id +".square.corner_1.x"),
                dungeonFile.getInt("room_"+ id +".square.corner_1.y"),
                dungeonFile.getInt("room_"+ id +".square.corner_1.z")));
            locations.add(new Location(SimpleDungeon.getInstance().getServer().getWorld(
                dungeonFile.getString("world")),
                dungeonFile.getInt("room_"+ id +".square.corner_2.x"),
                dungeonFile.getInt("room_"+ id +".square.corner_2.y"),
                dungeonFile.getInt("room_"+ id +".square.corner_2.z")));
        }
        disableLoot = dungeonFile.getBoolean("room_"+ id +".disable_loot");
        
        List<?> yamlList = dungeonFile.getList("room_" + id + ".commands");

        for (int i = 0; yamlList != null &&  i < yamlList.size(); i++) {
            if (yamlList.get(i) instanceof String) {
                commands.add((String)yamlList.get(i));
                Logger.instance.debugConsole("Command " + (String)yamlList.get(i) + " added upon room reset.");
            } else {
                Logger.instance.warning(yamlList.get(i).toString() + " is not considered as string.");
            }
        }
        Logger.instance.info("Room " + id + " loaded properly.");
    }

    public static boolean isWellFormated(YamlConfiguration dungeonFile, int id) {
        Logger.instance.debugConsole("room_" + id + ".shape : "  +  dungeonFile.getString("room_"+ id + ".shape"));
        if (dungeonFile.getString("room_"+ id + ".shape").equalsIgnoreCase("circle")) {
            if (!dungeonFile.contains("room_"+ id +".circle.center.x") &&
                !dungeonFile.contains("room_"+ id +".circle.center.y") &&
                !dungeonFile.contains("room_"+ id +".circle.center.z") &&
                !dungeonFile.contains("room_" + id + ".circle.radius")) {
                    return (false);
            }
        } else if (dungeonFile.getString("room_"+ id + "shape").equalsIgnoreCase("square")) {
            if (!dungeonFile.contains("room_" + id + ".square.corner_1.x") &&
                !dungeonFile.contains("room_" + id + ".square.corner_1.y") &&
                !dungeonFile.contains("room_" + id + ".square.corner_1.z") &&
                !dungeonFile.contains("room_" + id + ".square.corner_2.x") &&
                !dungeonFile.contains("room_" + id + ".square.corner_2.y") &&
                !dungeonFile.contains("room_" + id + ".square.corner_2.z")) {
                    return (false);
            }
        } else {
            return (false);
        }



        if (!dungeonFile.contains("room_"+ id +".commands") &&
            !dungeonFile.contains("room_" + id + ".disable_loot")) {
                return (false);
        }
        return (true);
    }
}
