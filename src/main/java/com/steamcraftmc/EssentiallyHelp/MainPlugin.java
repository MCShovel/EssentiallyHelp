package com.steamcraftmc.EssentiallyHelp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        File cFile = new File(getDataFolder(), "config.yml");
        if (!cFile.exists()) {
            cFile.getParentFile().mkdirs();
            createConfigFile(getResource("config.yml"), cFile);
            log(Level.INFO, "Configuration file config.yml created!");
        }

		new com.steamcraftmc.EssentiallyHelp.Commands.HelpCommand(this);
		_logger.log(Level.CONFIG, "Plugin listening for events.");
    }

    private void createConfigFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
        	log(Level.SEVERE, e.toString());
        }
    }
    
    @Override
    public void onDisable() {
    }
}
