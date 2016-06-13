package com.steamcraftmc.EssentiallyHelp.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.steamcraftmc.EssentiallyHelp.MainPlugin;

public class InfoCommand extends BaseCommand {

	Map<String, Map<Integer, String[]>> infoPages;
	Map<String, String> perms;

	public InfoCommand(MainPlugin plugin) {
		super(plugin, 
				plugin.getConfig().getConfigurationSection("info") == null
				? "essentials.info.undefined"
				: "essentials.info", "info", 0, 2);
	}
	
	boolean enabled() {
		
		if (infoPages != null)
			return infoPages.size() >= 0;

        ConfigurationSection info = plugin.getConfig().getConfigurationSection("info");
        infoPages = new HashMap<String, Map<Integer,String[]>>();
        perms = new HashMap<String, String>();

    	plugin.log(Level.FINE, "Loading topics...");
        try {
	        if (info != null) {
		        Set<String> custom = info.getKeys(false);
		        if (custom != null) {
			        for (String k : custom) {
			        	
			        	plugin.log(Level.FINE, "Loading topic: " + k);
			        	List<?> l = info.getStringList(k);
			        	String[] lines = l.toArray(new String[l.size()]);

			        	Map<Integer,String[]> pgs;
			        	if (infoPages.containsKey(k)) {
			        		pgs = infoPages.get(k.toLowerCase());
			        	}
			        	else {
			        		infoPages.put(k.toLowerCase(), pgs = new HashMap<Integer, String[]>());
			        	}
			        	
			        	String needsPerm = plugin.getConfig().getString("info." + k + ".permission");
			        	if (needsPerm != null && !needsPerm.equalsIgnoreCase("")) {
			        		perms.put(k.toLowerCase(), needsPerm);
				        	plugin.log(Level.FINE, "Permission for " + k + " added: " + needsPerm);
			        	}
			        	
			        	if (lines.length == 0) {
				        	plugin.log(Level.FINE, "Loading topic: " + k);
			        		for (int ix = 1; ix < 100; ix++) {
			        			l = plugin.getConfig().getStringList("info." + k + "." + String.valueOf(ix));
					        	lines = l.toArray(new String[l.size()]);
					        	if (lines.length > 0) {
					        		pgs.put(ix, lines);
					        	}
					        	else {
					        		break;
					        	}
			        		}
			        	} else {
			        		pgs.put(1, lines);
			        	}
			        }
		        }
	        }
        }
        catch(Exception e) {
        	e.printStackTrace();
        }

    	this.plugin.getCommand(this.cmdName).setPermission(
    			infoPages.size() == 0
    				? "essentials.info.undefined"
    				: "essentials.info"
    			);
        
    	plugin.log(Level.INFO, "Loaded topics: " + String.valueOf(infoPages.size()));
		return infoPages.size() >= 0;
	}	
	
	@Override
	protected boolean doPlayerCommand(Player user, Command cmd, String[] args) throws Exception {

        if (!enabled()) {
        	return false;
        }
		String topic = (args.length > 0 ? args[0] : "").toLowerCase();
		String pgText = args.length > 1 ? args[1] : "";
		int pgNum = 1;
		
		if (topic.matches("^\\d+$")) {
			pgText = topic;
			topic = "";
		}
		if (pgText.length() > 0 && pgText.matches("^\\d+$")) {
			pgNum = Integer.parseInt(pgText);
		}
		
        if (topic.equalsIgnoreCase("reload") && user.hasPermission("*")) {
    		plugin.reloadConfig();
    		infoPages = null;
    		user.sendMessage(ChatColor.GOLD + "EssentiallyHelp configuration reloaded.");
    		return true;
        }
        
        if (perms.containsKey(topic) && !user.hasPermission(perms.get(topic))) {
        	user.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have access to that topic."));
        	return true;
        }
        
        if (topic.equalsIgnoreCase("")) {
        	List<String> topics = new ArrayList<String>(infoPages.keySet());
        	Collections.sort(topics, String.CASE_INSENSITIVE_ORDER);
        	for (int i = topics.size()-1; i >= 0; i--) {
                if (perms.containsKey(topics.get(i)) && !user.hasPermission(perms.get(topics.get(i)))) {
            		topics.remove(i);
                }
        	}
        	
        	user.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Topics&f: " + String.join(",  ", topics)));
        }
        else if (infoPages.containsKey(topic.toLowerCase())) {
        	Map<Integer, String[]> pages = infoPages.get(topic);
        	if (pages.containsKey(pgNum)) {
        		String[] lines = pages.get(pgNum);
            	user.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", lines)).replace("& ", "&"));
        	}
        	else {
            	user.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Topic &c" + topic + "&6 page &c" + pgNum + "&6 not found."));
        	}
        }
        else {
        	user.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Topic &c" + topic + "&6 not found."));
        }

        return true;
	}

}
