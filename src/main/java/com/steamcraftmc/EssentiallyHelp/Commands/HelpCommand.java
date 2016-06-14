package com.steamcraftmc.EssentiallyHelp.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.steamcraftmc.EssentiallyHelp.MainPlugin;
import com.steamcraftmc.EssentiallyHelp.Utils.HelpBuilder;
import com.steamcraftmc.EssentiallyHelp.Utils.TextPager;

public class HelpCommand extends BaseCommand {
	
	public HelpCommand(MainPlugin plugin) {
		super(plugin, "essentials.help", "help", 0, 2);
	}

	@Override
	protected boolean doPlayerCommand(Player user, Command cmd, String[] args) throws Exception {
        
        String pageStr = args.length > 0 ? args[0] : null;

        if (pageStr != null && pageStr.equalsIgnoreCase("reload") && user.hasPermission("*")) {
    		plugin.reloadConfig();
    		user.sendMessage(ChatColor.GOLD + "EssentiallyHelp configuration reloaded.");
    		return true;
        }
        return doCommand(user, cmd, args);
	}

	@Override
    protected boolean doConsoleCommand(CommandSender sender, Command cmd, String[] args) throws Exception {
        
        String pageStr = args.length > 0 ? args[0] : null;

        if (pageStr != null && pageStr.equalsIgnoreCase("reload")) {
    		plugin.reloadConfig();
    		sender.sendMessage(ChatColor.GOLD + "EssentiallyHelp configuration reloaded.");
    		return true;
        }
        return doCommand(sender, cmd, args);
	}


    protected boolean doCommand(CommandSender sender, Command cmd, String[] args) throws Exception {

        String pageStr = args.length > 0 ? args[0] : null;
        String chapterPageStr = args.length > 1 ? args[1] : null;
        String command = cmdName;

        HelpBuilder output;
		if (pageStr == null || pageStr.matches("^\\d+$")) {
            output = new HelpBuilder(plugin, sender, "");
        } else {
            if (pageStr.length() > 26) {
                pageStr = pageStr.substring(0, 25);
            }
            output = new HelpBuilder(plugin, sender, pageStr.toLowerCase());
            command = command.concat(" ").concat(pageStr);
            pageStr = chapterPageStr;
        }
        chapterPageStr = null;

        final TextPager pager = new TextPager(plugin, output);
        pager.showPage(pageStr, chapterPageStr, command, sender);

    	return true;
    }
}
