package fr.dzious.bukkit.simpledungeon.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.plugin.Dungeon;
import fr.dzious.bukkit.simpledungeon.plugin.Gate;
import fr.dzious.bukkit.simpledungeon.utils.Logger;
import fr.dzious.bukkit.simpledungeon.utils.Utils;

public class CommandDungeon implements CommandExecutor, TabCompleter {

    private final String commandName = "Dungeon";
    private Map<String, List<String>> tabComplete = new HashMap<>();

    public CommandDungeon () {
        initTabComplete();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.isOp())
            return (false); 

        Logger.instance.debugConsole("args.length = " + args.length);
        for (int i = 0; i < args.length; i++)
            Logger.instance.debugConsole("args[" + i + "] = " + args[i]);

        if (args.length == 2 && (args[1].equalsIgnoreCase("start") || args[1].equalsIgnoreCase("reset") || args[1].equalsIgnoreCase("reload") )) {
            if (SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]) == null) {
                sender.sendMessage("Error while using " + args[1] + " command. Specified dungeon does not exists");
            } else if (args[1].equalsIgnoreCase("start")) {
                Logger.instance.debugConsole("Start command");
                SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).start();
            } else if (args[1].equalsIgnoreCase("reset")) {
                Logger.instance.debugConsole("Reset command");
                SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).reset();
            } else if (args[1].equalsIgnoreCase("reload")) {
                Logger.instance.debugConsole("Reload command");
                SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).reload();
            } 
            return (true);
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("open") || args[1].equalsIgnoreCase("close"))) {
            if (SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]) == null || SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).getGate(Integer.parseInt(args[2])) == null ) {
                sender.sendMessage("Error while using " + args[1] + " command. Specified dungeon or gate does not exists");
            } else if (!SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).isRunning()) {
                sender.sendMessage("Error while using " + args[1] + " command. Dungeon is not running");
            } else if (args[1].equalsIgnoreCase("open")) {
                Logger.instance.debugConsole("Open command");
                Dungeon d = SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]);
                Gate g = d.getGate(Integer.parseInt(args[2]));
                g.open();
                d.resetRoom(g.getResetRoomId());
            } else if (args[1].equalsIgnoreCase("close")) {
                Logger.instance.debugConsole("Close command");
                SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).getGate(Integer.parseInt(args[2])).close(true);
            }
            return (true);
        } else {
            return (false);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String concatedCommand = Utils.concatCommand(commandName, args, ".");

        if (tabComplete.containsKey(concatedCommand))
            return (tabComplete.get(concatedCommand));
        else 
            return null;
    }

    private void initTabComplete() {
        tabComplete.put(commandName.toLowerCase(), SimpleDungeon.getInstance().getDungeonManager().getDungeonNames());
        for (String dungeonName : SimpleDungeon.getInstance().getDungeonManager().getDungeonNames()) {
            tabComplete.put(commandName.toLowerCase() + "." + dungeonName, Arrays.asList("start", "open"));
            tabComplete.put(commandName.toLowerCase() + "." + dungeonName + ".open" , SimpleDungeon.getInstance().getDungeonManager().getDungeon(dungeonName).getGatesIds());
        }
    }
}