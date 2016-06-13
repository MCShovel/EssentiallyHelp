package com.steamcraftmc.EssentiallyHelp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
 
public class MainPlugin extends JavaPlugin {
	public final   Logger  _logger;
	public Boolean _exLogging;

	public MainPlugin() {
		_exLogging = true;
		_logger = getLogger();
		_logger.setLevel(Level.ALL);
		_logger.log(Level.CONFIG, "Plugin initializing...");
		
		this.getConfig().options().copyDefaults(true);
	}

	public void log(Level level, String text) {
		_logger.log(Level.INFO, text);
	}

    @Override
    public void onEnable() {
		new com.steamcraftmc.EssentiallyHelp.Commands.HelpCommand(this);
    	
		_logger.log(Level.CONFIG, "Plugin listening for events.");
    }

    @Override
    public void onDisable() {
    }
}
