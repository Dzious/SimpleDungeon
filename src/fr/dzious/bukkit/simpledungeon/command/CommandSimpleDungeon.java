package fr.dzious.bukkit.simpledungeon.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import fr.dzious.bukkit.simpledungeon.SimpleDungeon;
import fr.dzious.bukkit.simpledungeon.utils.Utils;

public class CommandSimpleDungeon implements CommandExecutor, TabCompleter {

    private final String commandName = "SimpleDungeon";
    private Map<String, List<String>> tabComplete = new HashMap<>();

    public CommandSimpleDungeon () {
        initTabComplete();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String concatedCommand = Utils.concatCommand(commandName, args, ".");

        if (tabComplete.containsKey(concatedCommand))
            return (tabComplete.get(concatedCommand));
        else
            return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            SimpleDungeon.getInstance().reload();
            return (true);
        } else if (args.length == 2 && SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[1]) != null) {
            SimpleDungeon.getInstance().getDungeonManager().getDungeon(args[1]).reload();
            return (true);
        } else {
            return (false);
        }
    }

    private void initTabComplete() {
        tabComplete.put(commandName.toLowerCase(), Arrays.asList("reload"));
        tabComplete.put(commandName.toLowerCase() + ".reload", SimpleDungeon.getInstance().getDungeonManager().getDungeonNames());
    }
}