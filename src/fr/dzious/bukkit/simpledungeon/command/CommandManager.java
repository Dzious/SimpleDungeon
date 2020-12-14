package fr.dzious.bukkit.simpledungeon.command;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Logger;

public class CommandManager {

    private SimpleDungeon plugin;

    public CommandManager(SimpleDungeon plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        Logger.instance.info("CommandManager OnEnable");
        // plugin.getCommand("SimpleDungeon").setExecutor(new CommandSimpleDungeon());
        plugin.getCommand("Dungeon").setExecutor(new CommandDungeon());
    }
}
