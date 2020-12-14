package com.dzious.bukkit.template.utils;

import java.util.logging.Logger;

import com.dzious.bukkit.template.Template;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LogManager {

    private Template plugin;
    private Logger logger;
    private boolean debugEnable = false;
    private String debugPlayer = null;

    public LogManager(Template plugin, Logger logger, ConfigManager configManager) {
        this.plugin = plugin;
        this.logger = logger;
        if (configManager.doPathExist("debug.enable"))
            this.debugEnable = configManager.getBooleanFromPath("debug.enable");
        if (configManager.doPathExist("debug.player"))
            this.debugPlayer = configManager.getStringFromPath("debug.player");
    }

    public void logInfo(String msg) {
        logger.info(msg);
    }

    public void logWarning(String msg) {
        logger.warning(msg);
    }

    public void logSevere(String msg) {
        logger.severe(msg);
    }

    public void logDebugConsole(String msg) {
        if (isDebugEnable() == true)
            logger.info("[" + ChatColor.RED + plugin.getName() + ChatColor.WHITE + "]" + ChatColor.BLUE + "[Debug]" + ChatColor.WHITE + " : "  + msg);
    }

    public void logDebugPlayer(String msg) {
        if (isDebugEnable() == true && getDebugPlayer() != null)
            for (Player p : plugin.getServer().getOnlinePlayers())
                if (p.getName() == getDebugPlayer())
                    p.sendMessage("[" + ChatColor.RED + plugin.getName() + ChatColor.WHITE + "]" + ChatColor.BLUE + "[Debug]" + ChatColor.WHITE + " : "  + msg);
            }

    public void logException (Exception e) {
        if (isDebugEnable() == true)
            e.printStackTrace();
    }

    private boolean isDebugEnable() {
        return (debugEnable);
    }

    private String getDebugPlayer() {
        return (debugPlayer);
    }
}
