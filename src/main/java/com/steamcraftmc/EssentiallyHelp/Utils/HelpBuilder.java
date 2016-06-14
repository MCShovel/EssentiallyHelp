package com.steamcraftmc.EssentiallyHelp.Utils;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.steamcraftmc.EssentiallyHelp.MainPlugin;

import java.util.*;
import java.util.logging.Level;
import java.io.IOException;


public class HelpBuilder implements IText {
    private static final String DESCRIPTION = "description";
    private static final String PERMISSION = "permission";
    private static final String PERMISSIONS = "permissions";
    private final MainPlugin plugin;
    private final transient List<String> lines = new ArrayList<String>();
    private final transient List<String> chapters = new ArrayList<String>();
    private final transient Map<String, Integer> bookmarks = new HashMap<String, Integer>();

    @SuppressWarnings("unchecked")
	public HelpBuilder(MainPlugin plugin, final CommandSender user, final String match) throws IOException {
    	this.plugin = plugin;
        boolean reported = false;
        final List<String> newLines = new ArrayList<String>();
        final HashSet<String> known = new HashSet<>();
        String pluginName = "";
        String pluginNameLow = "";

        boolean pluginsOnly = match.equalsIgnoreCase("plugins") && user.hasPermission("*");
        
        if (!match.equalsIgnoreCase("")) {
            lines.add(helpMatching(match));
        }
        else if (!pluginsOnly) {
	        FileConfiguration config = plugin.getConfig();
	        ConfigurationSection cmds = config.getConfigurationSection("commands");
	        if (cmds != null) {
		        Set<String> custom = cmds.getKeys(false);
		        if (custom != null) {
			        for (String k : custom) {
			        	if (!cmds.isString(k)) continue;
			        	String val = cmds.getString(k);
			            newLines.add(helpLine(k, val));
			        }
		        }
	        }
        }
        
        for (Plugin p : plugin.getServer().getPluginManager().getPlugins()) {
            try {
                final List<String> pluginLines = new ArrayList<String>();
                final PluginDescriptionFile desc = p.getDescription();
                final Map<String, Map<String, Object>> cmds = desc.getCommands();
                pluginName = p.getDescription().getName();
                pluginNameLow = pluginName.toLowerCase(Locale.ENGLISH);
                
                if (isPluginDisabled(pluginNameLow)) {
                	continue;
                }
            	if (pluginsOnly) {
                    lines.add(pluginList(pluginName, pluginNameLow, desc.getVersion()));
                    String url = desc.getWebsite(); 
                	if (url != null && url.length() > 0) {
                		lines.add(ChatColor.DARK_GRAY + url);
                	}
                    continue;
            	}
                if (pluginNameLow.equals(match)) {
                    lines.clear();
                    newLines.clear();
                    lines.add(helpFrom(p.getDescription().getName()));
                }
            	
                for (Map.Entry<String, Map<String, Object>> k : cmds.entrySet()) {
                    try {
                        if (!match.equalsIgnoreCase("") && (!pluginNameLow.contains(match)) && 
                        		(!k.getKey().toLowerCase(Locale.ENGLISH).contains(match)) && 
                        		(!(k.getValue().get(DESCRIPTION) instanceof String && 
                				((String) k.getValue().get(DESCRIPTION)).toLowerCase(Locale.ENGLISH).contains(match)))) {
                            continue;
                        }
                        if (known.contains(k.getKey())) {
                        	continue;
                        }
                        known.add(k.getKey());

                        if (pluginNameLow.contains("essentials")) {
                            final String node = "essentials." + k.getKey();
                            if (!isCommandDisabled(k.getKey()) && user.hasPermission(node)) {
                                pluginLines.add(helpLine(k.getKey(), k.getValue().get(DESCRIPTION)));
                            }
                        } else {
                            final Map<String, Object> value = k.getValue();
                            Object permissions = null;
                            if (value.containsKey(PERMISSION)) {
                                permissions = value.get(PERMISSION);
                            } else if (value.containsKey(PERMISSIONS)) {
                                permissions = value.get(PERMISSIONS);
                            }
                            if (user.hasPermission("essentials.help." + pluginNameLow + "." + k.getKey())) {
                                pluginLines.add(helpLine(k.getKey(), value.get(DESCRIPTION)));
                            } else if (permissions instanceof List<?> && !((List<Object>) permissions).isEmpty()) {
                                boolean enabled = false;
                                for (Object o : (List<Object>) permissions) {
                                    if (o instanceof String && user.hasPermission(o.toString())) {
                                        enabled = true;
                                        break;
                                    }
                                }
                                if (enabled) {
                                    pluginLines.add(helpLine(k.getKey(), value.get(DESCRIPTION)));
                                }
                            } else if (permissions instanceof String && !"".equals(permissions)) {
                                if (user.hasPermission(permissions.toString())) {
                                    pluginLines.add(helpLine(k.getKey(), value.get(DESCRIPTION)));
                                }
                            } else {
                                pluginLines.add(helpLine(k.getKey(), value.get(DESCRIPTION)));
                            }
                        }
                    }
                    catch (NullPointerException ex) {
                    }
                }
                if (!pluginLines.isEmpty()) {
                    newLines.addAll(pluginLines);
                    if (pluginNameLow.equals(match)) {
                        break;
                    }
                    if (showPlugins() && match.equalsIgnoreCase("")) {
                        lines.add(helpPlugin(pluginName, pluginNameLow));
                    }
                }
            } catch (NullPointerException ex) {
            } catch (Exception ex) {
                if (!reported) {
                    plugin.log(Level.WARNING, ex.toString());
                }
                reported = true;
            }
        }
        
        if (sorted()) {
        	Collections.sort(newLines, String.CASE_INSENSITIVE_ORDER);
        }
        
        lines.addAll(newLines);
    }

    public String helpMatching(String name) {
    	String msg = plugin.getConfig().getString("formatting.helpMatching", "&6Commands  matching \"&c{name}&6\":");
    	msg = msg.replace("{name}", name);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String pluginList(String name, String lname, String ver) {
    	
    	String msg = plugin.getConfig().getString("formatting.pluginList", "&c/help {lname}&f: {name} v{ver}");
    	msg = msg.replace("{name}", name).replace("{lname}", lname).replace("{ver}", ver == null ? "?" : ver);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    public String helpPlugin(String name, String lname) {
    	String msg = plugin.getConfig().getString("formatting.helpPlugin", "&4{name}&f: Plugin Help: /help {lname}");
    	msg = msg.replace("{name}", name).replace("{lname}", lname);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String helpFrom(String name) {
    	String msg = plugin.getConfig().getString("formatting.helpFrom", "&6Commands from {name}:");
    	msg = msg.replace("{name}", name);
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    public String helpLine(String name, Object desc) {
    	String msg = plugin.getConfig().getString("formatting.helpLine", "&6/{name}&f: {desc}");
    	msg = msg.replace("{name}", name).replace("{desc}", String.valueOf(desc));
    	return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public boolean sorted() {
    	return plugin.getConfig().getBoolean("options.sorted", false);
    }

    public boolean showPlugins() {
    	return plugin.getConfig().getBoolean("options.showplugins", false);
    }

    public boolean isCommandDisabled(String name) {
    	return plugin.getConfig().getBoolean("disabled.command." + name.toLowerCase(), false);
    }

    public boolean isPluginDisabled(String name) {
    	return plugin.getConfig().getBoolean("disabled.plugin." + name.toLowerCase(), false);
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> getChapters() {
        return chapters;
    }

    public Map<String, Integer> getBookmarks() {
        return bookmarks;
    }
}
