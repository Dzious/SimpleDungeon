package fr.dzious.bukkit.simpledungeon.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandDungeon implements CommandExecutor, TabCompleter {

    private final String commandName = "Dungeon";
    private Map<String, List<String>> tabComplete = new HashMap<>();

    public CommandDungeon () {
        initTabComplete();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("start")) {
            SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).start();
            return (true);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("open") && SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).getGate(Integer.parseInt(args[2])) != null) {
            SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[0]).getGate(Integer.parseInt(args[2])).open();
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