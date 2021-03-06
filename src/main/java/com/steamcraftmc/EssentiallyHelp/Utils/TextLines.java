package com.steamcraftmc.EssentiallyHelp.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

public class TextLines implements IText {

	private final transient List<String> lines;
    private final transient List<String> chapters = new ArrayList<String>();
    private final transient Map<String, Integer> bookmarks = new HashMap<String, Integer>();

	public TextLines(List<String> lines) {
		this.lines = new ArrayList<String>(lines);
	}
	
	@Override
	public List<String> getLines() {
		return lines;
	}

	@Override
	public List<String> getChapters() {
		return chapters;
	}

	@Override
	public Map<String, Integer> getBookmarks() {
		return bookmarks;
	}

	public IText applyColor() {
		for (int ix=0; ix < lines.size(); ix++) {
			lines.set(ix, ChatColor.translateAlternateColorCodes('&', lines.get(ix)));
		}
		return this;
	}

}
