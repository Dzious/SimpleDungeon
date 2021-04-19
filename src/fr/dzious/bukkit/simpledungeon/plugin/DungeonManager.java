package fr.dzious.bukkit.simpledungeon.plugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Logger;
import net.md_5.bungee.api.ChatColor;

public class DungeonManager {
    Map<String, Dungeon> dungeons = new HashMap<>();

    public DungeonManager() {
        reload();
    }

    public boolean loadDungeon(String dungeonName) {

        if (dungeons.containsKey(dungeonName)) {
            Logger.instance.warning("Dungeon : " + ChatColor.BOLD + dungeonName + ChatColor.RESET + " is already loaded or has the same name than an other dungeon.");
            return (false);
        }

        if (Files.notExists(Paths.get(SimpleDungeon.getInstance().getDataFolder().toString() + "/dungeons/" + dungeonName + ".yml"))) {
            Logger.instance.warning("Dungeon : " + ChatColor.BOLD + dungeonName + ChatColor.RESET + " does not exists.");
            Logger.instance.warning("File name should be " + ChatColor.GREEN + dungeonName + ".yml");
            return (false);
        }
        
        if (!Dungeon.isWellFormated(dungeonName)) {
            Logger.instance.warning("Dungeon : " + ChatColor.BOLD + dungeonName + ChatColor.RESET + " has missing elements.");
            return (false);
        }
        
        dungeons.put(dungeonName, new Dungeon(dungeonName));
        
        return (true);
    }

    public Dungeon getDungeon(String dungeonName) {
        return (dungeons.get(dungeonName));
    }
    
    public List<String> getDungeonNames() {
        List<String> dungeonsNames = new ArrayList<>();
    
        for (Map.Entry<String,Dungeon> dungeon : dungeons.entrySet()) {
            dungeonsNames.add(dungeon.getKey());
        }
        return (dungeonsNames);
    }

    public void reload() {
        List<?> yamlList = SimpleDungeon.configManager.getList("dungeons");
        
        if (yamlList == null) {
            Logger.instance.error("\"dungeons does not exists in config or is wrongly formated");
            return;
        }

        for (int i = 0; i < yamlList.size(); i++) {
            if (yamlList.get(i) instanceof String) {
                loadDungeon((String) yamlList.get(i));
            } else {
                Logger.instance.error(yamlList.get(i).toString() + " is not a valid dungeon name.");
            }
        }
    }
}
