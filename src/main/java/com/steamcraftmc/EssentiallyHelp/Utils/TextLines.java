package com.steamcraftmc.EssentiallyHelp.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextLines implements IText {

	private final transient List<String> lines;
    private final transient List<String> chapters = new ArrayList<String>();
    private final transient Map<String, Integer> bookmarks = new HashMap<String, Integer>();

	public TextLines(List<String> lines) {
		this.lines = lines;
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

}
