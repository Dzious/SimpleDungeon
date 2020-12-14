package fr.dzious.bukkit.simpledungeon;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.dzious.bukkit.simpledungeon.command.CommandManager;
import fr.dzious.bukkit.simpledungeon.plugin.DungeonManager;
import fr.dzious.bukkit.simpledungeon.utils.Logger;
import fr.dzious.bukkit.simpledungeon.utils.Utils;

public class SimpleDungeon extends JavaPlugin {

    public static SimpleDungeon INSTANCE;
    public static YamlConfiguration configManager;
    public static YamlConfiguration languageManager;
    private CommandManager commandManager;
    private DungeonManager dungeonManager;

    @Override
    public void onLoad() {
        return;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        Logger.instance.info("Simple Dungeon starting...");
        try {
            createConfigManager();
            Logger.instance.init();
            createLanguageManager();
            dungeonManager = new DungeonManager();
            commandManager = new CommandManager(INSTANCE);
            commandManager.onEnable();
            Logger.instance.info("Simple Dungeon started !");
        } catch (Exception e) {
            java.util.logging.Logger.getLogger("Minecraft").info("exception");
            Logger.instance.error("Unable to load Config or Language file.");
            e.printStackTrace();
            disablePlugin();
        }
        return;
    }

    @Override
    public void onDisable() {
        Logger.instance.info("Simple Dungeon stopping...");
        Logger.instance.info("Simple Dungeon stopped !");
        return;
    }

    public void disablePlugin() {
        Logger.instance.error("This is a fatal error, disabling Simple Dungeon");
        setEnabled(false);
    }

    public static SimpleDungeon getInstance() {
        return (INSTANCE);
    }

    public YamlConfiguration getConfigManager() {
        return (configManager);
    }

    public DungeonManager getDungeonManager() {
        return (dungeonManager);
    }

    public void reload() {
        try {
            reloadConfigManager();
            reloadLanguageManager();
            dungeonManager.reload();
        } catch (Exception e) {
            Logger.instance.warning("Unable to Load config or language file. Reload failed.");
            Logger.instance.exception(e);
        }
    }

    private void createConfigManager() throws Exception {
        configManager = new YamlConfiguration();
        File configFile = new File(getDataFolder() + "/config.yml");

        Logger.instance.debugConsole("exists : " + configFile.exists());
        Logger.instance.debugConsole("Path : " + getDataFolder() + "/config.yml");
        

        if (!configFile.exists())
            Utils.copy("/config.yml", getDataFolder() + "/config.yml");
        configManager.load(getDataFolder() + "/config.yml");
    }

    private void reloadConfigManager() throws Exception {
        File configFile = new File(getDataFolder() + "/config.yml");
        if (configFile.exists())
            configManager.load(getDataFolder() + "/config.yml");
    }

    private void createLanguageManager() throws Exception {
        languageManager = new YamlConfiguration();
        String languageFileString = "en_us.yml"; 
        
        if (configManager.contains("language"))
            languageFileString = configManager.getString("language");

        File languageFile  = new File(getDataFolder() + "/" + languageFileString);

        if (!languageFile.exists())
            // Utils.ExportResource("Languages/" + languageFileString);
            Utils.copy("/Languages/" + languageFileString, getDataFolder() + "/" + languageFileString);
        
        languageManager.load(getDataFolder() + "/" + languageFileString);
    }

    private void reloadLanguageManager() throws Exception {
        String languageFileString = "en_us.yml"; 
        
        if (configManager.contains("language"))
            languageFileString = configManager.getString("language");

        File languageFile  = new File(getDataFolder() + "/" + languageFileString);
        if (languageFile.exists())
            languageManager.load(getDataFolder() + "/" + languageFileString);
    }
    
}
