package com.steamcraftmc.EssentiallyHelp;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.steamcraftmc.EssentiallyHelp.Utils.KeywordReplacer;
import com.steamcraftmc.EssentiallyHelp.Utils.TextLines;
import com.steamcraftmc.EssentiallyHelp.Utils.TextPager;

public class PluginEventListener implements Listener {
	private final MainPlugin  plugin;

	public PluginEventListener(MainPlugin plugin) {
		this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		if (!plugin.getConfig().getBoolean("messages.join", true)) {
			event.setJoinMessage(null);
		}
		Player player = event.getPlayer();
		try {
			List<String> lines = plugin.getConfig().getStringList("messages." + player.getWorld().getName().toLowerCase());
			if (lines == null || lines.size() == 0) {
				lines = plugin.getConfig().getStringList("messages.welcome");
			}
			if (lines != null && lines.size() > 0) {
				new TextPager(plugin, 
						new KeywordReplacer(new TextLines(lines).applyColor(), player)
				).showDirect(player);
			}
		}
		catch(Exception e) {
			plugin.log(Level.SEVERE, e.toString());
		}
    }

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if (!plugin.getConfig().getBoolean("messages.quit", true)) {
			event.setQuitMessage(null);
		}
    }
}
