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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.steamcraftmc.EssentiallyHelp.MainPlugin;
import com.steamcraftmc.EssentiallyHelp.Utils.TextLines;
import com.steamcraftmc.EssentiallyHelp.Utils.TextPager;

public class InfoCommand extends BaseCommand {

	Map<String, List<String>> infoTopics;
	Map<String, String> perms;

	public InfoCommand(MainPlugin plugin) {
		super(plugin, 
				plugin.getConfig().getConfigurationSection("info") == null
				? "essentials.info.undefined"
				: "essentials.info", "info", 0, 2);
	}
	
	boolean enabled() {
		
		if (infoTopics != null)
			return infoTopics.size() >= 0;

        ConfigurationSection info = plugin.getConfig().getConfigurationSection("info");
        infoTopics = new HashMap<String, List<String>>();
        perms = new HashMap<String, String>();

    	plugin.log(Level.FINE, "Loading topics...");
        try {
	        if (info != null) {
		        Set<String> custom = info.getKeys(false);
		        if (custom != null) {
			        for (String k : custom) {
			        	ArrayList<String> lines = new ArrayList<String>();
			        	List<String> inputLines = plugin.getConfig().getStringList("info." + k + ".text");
			        	if (inputLines == null || inputLines.size() == 0) {
			        		plugin.log(Level.SEVERE, "Configuration error, missing node 'info." + k + ".text'!");
			        		continue;
			        	}
			        	for (String line : inputLines) {
			        		lines.add(ChatColor.translateAlternateColorCodes('&', line).replace("& ", "&"));
			        	}
			        	infoTopics.put(k.toLowerCase(), lines);

			        	String needsPerm = plugin.getConfig().getString("info." + k + ".permission");
			        	if (needsPerm != null && !needsPerm.equalsIgnoreCase("")) {
			        		perms.put(k.toLowerCase(), needsPerm);
				        	plugin.log(Level.FINE, "Permission for " + k + " added: " + needsPerm);
			        	}
			        }
		        }
	        }
        }
        catch(Exception e) {
        	e.printStackTrace();
        }

    	this.plugin.getCommand(this.cmdName).setPermission(
    			infoTopics.size() == 0
    				? "essentials.info.undefined"
    				: "essentials.info"
    			);
        
    	plugin.log(Level.INFO, "Loaded topics: " + String.valueOf(infoTopics.size()));
		return infoTopics.size() >= 0;
	}	

	@Override
	protected boolean doPlayerCommand(Player user, Command cmd, String[] args) throws Exception {
		return doCommand(user, cmd, args);
	}

	@Override
	protected boolean doConsoleCommand(CommandSender user, Command cmd, String[] args) throws Exception {
		return doCommand(user, cmd, args);
	}
	
	protected boolean doCommand(CommandSender user, Command cmd, String[] args) throws Exception {

        if (!enabled()) {
        	return false;
        }
		String topic = (args.length > 0 ? args[0] : "").toLowerCase();
		String pgText = args.length > 1 ? args[1] : "";
		
		if (topic.matches("^\\d+$")) {
			pgText = topic;
			topic = "";
		}
		
        if (topic.equalsIgnoreCase("reload") && user.hasPermission("*")) {
    		plugin.reloadConfig();
    		infoTopics = null;
    		perms = null;
    		user.sendMessage(ChatColor.GOLD + "EssentiallyHelp configuration reloaded.");
    		return true;
        }
        
        if (perms.containsKey(topic) && !user.hasPermission(perms.get(topic))) {
        	user.sendMessage(noAccessToTopic());
        	return true;
        }
        
        if (topic.equalsIgnoreCase("")) {
        	List<String> topics = new ArrayList<String>(infoTopics.keySet());
        	Collections.sort(topics, String.CASE_INSENSITIVE_ORDER);
        	for (int i = topics.size()-1; i >= 0; i--) {
                if (perms.containsKey(topics.get(i)) && !user.hasPermission(perms.get(topics.get(i)))) {
            		topics.remove(i);
                }
        	}
        	
        	user.sendMessage(topicList(String.join(", ", topics)));
        }
        else if (infoTopics.containsKey(topic.toLowerCase())) {
        	List<String> lines = infoTopics.get(topic);

        	boolean onepage = pgText.equals("*");
            final TextPager pager = new TextPager(plugin, new TextLines(lines), onepage);
            pager.showPage(onepage ? "" : pgText, topic, "info " + topic, user);
            return true;
        }
        else {
        	user.sendMessage(topicNotFound(topic));
        }

        return true;
	}

    public String noAccessToTopic() {
    	String msg = plugin.getConfig().getString("formatting.noAccessToTopic", "&cYou do not have access to that topic.");
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String topicNotFound(String topic) {
    	String msg = plugin.getConfig().getString("formatting.topicNotFound", "&6Topic &c{topic}&6 not found.");
    	msg = msg.replace("{topic}", topic);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String topicList(String topics) {
    	String msg = plugin.getConfig().getString("formatting.topicList", "&6Topics&f: {topics}");
    	msg = msg.replace("{topics}", topics);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }


}
