package com.steamcraftmc.EssentiallyHelp.Commands;

import org.bukkit.command.Command;
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
        String chapterPageStr = args.length > 1 ? args[1] : null;
        String command = cmdName;

        HelpBuilder output;
		if (pageStr == null || pageStr.matches("^\\d+$")) {
            output = new HelpBuilder(plugin, user, "");
        } else {
            if (pageStr.length() > 26) {
                pageStr = pageStr.substring(0, 25);
            }
            output = new HelpBuilder(plugin, user, pageStr.toLowerCase());
            command = command.concat(" ").concat(pageStr);
            pageStr = chapterPageStr;
        }
        chapterPageStr = null;

        final TextPager pager = new TextPager(plugin, output);
        pager.showPage(pageStr, chapterPageStr, command, user);
        return true;
	}

}
