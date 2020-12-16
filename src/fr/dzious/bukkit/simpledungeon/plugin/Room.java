package fr.dzious.bukkit.simpledungeon.plugin;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;

public class Room {
    Location location;
    String command;
    int size;
    boolean disableLoot;

    Room(YamlConfiguration dungeonFile, int id) {
        reload(dungeonFile, id);
    }

    public void reset(World dungeonWorld, Location dungeonEntry, Map<String, String> titles) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(location) <= size) {
                p.sendTitle(titles.get("title"),titles.get("subtitle"), 5, 10, 5);
                p.teleport(dungeonEntry);
            }
        }
        boolean doMobLoot = dungeonWorld.getGameRuleValue(GameRule.DO_MOB_LOOT);
        if (disableLoot && doMobLoot)
            dungeonWorld.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        Bukkit.dispatchCommand(SimpleDungeon.getInstance().getServer().getConsoleSender(), command);
        if (disableLoot && doMobLoot)
            dungeonWorld.setGameRule(GameRule.DO_ENTITY_DROPS, true);
    }

    public void reload(YamlConfiguration dungeonFile, int id) {
        location = new Location(SimpleDungeon.getInstance().getServer().getWorld(
        dungeonFile.getString("world")),
        dungeonFile.getInt("room_"+ id +".location.x"),
        dungeonFile.getInt("room_"+ id +".location.y"),
        dungeonFile.getInt("room_"+ id +".location.z"));
        command = dungeonFile.getString("room_"+ id +".command");
        size = dungeonFile.getInt("room_"+ id +".size");
        disableLoot = dungeonFile.getBoolean("room_"+ id +".disable_loot");
    }

    public static boolean isWellFormated(YamlConfiguration dungeonFile, int id) {
        if (!dungeonFile.contains("room_"+ id +".location.x") &&
            !dungeonFile.contains("room_"+ id +".location.y") &&
            !dungeonFile.contains("room_"+ id +".location.z") &&
            !dungeonFile.contains("room_"+ id +".command") &&
            !dungeonFile.contains("room_" + id + ".size") &&
            !dungeonFile.contains("room_" + id + ".disable_loot")) {
                return (false);
        }
        return (true);
    }
}
