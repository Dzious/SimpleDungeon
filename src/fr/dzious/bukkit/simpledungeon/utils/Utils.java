package fr.dzious.bukkit.simpledungeon.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;

public class Utils {
    private Utils() {}

    public static String concatCommand(String label, String[] args, String delimiter) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(label);
        for (String arg : args) {
            sb.append(delimiter);
            sb.append(arg);
        }
        return (sb.toString());
    }

    public static boolean copy(String source , String destination) {
        boolean success = true;

        Logger.instance.debugConsole("Copying : " + source + " to : " + destination);

        try {
            InputStream stream = SimpleDungeon.getInstance().getClass().getResourceAsStream(source);
            if (stream != null) {
                Files.copy(stream, Paths.get(destination));
                stream.close();
            } else
                Logger.instance.debugConsole("Source stream is null");
        } catch (IOException e) {
            Logger.instance.warning("Unable to copy : " + source + " to : " + destination);
            success = false;
        }

        return (success);
    }

    private static String getClosestPlayer(Location l) {
        String name = "";
        int distance = -1;

        for (Player p : l.getWorld().getPlayers()) {
            if (distance == -1 || p.getLocation().distance(l) < distance)
                name = p.getName();
        }
        return (name);
    }

    public static String formatCommand (String command, Location l) {
        if (command.contains("{player}")) {
            String name = getClosestPlayer(l);
            Logger.instance.debugConsole("Closest Player is : " + name);
            command = command.replaceAll("\\{player\\}", name);
        }
        return (command);
    }
}
