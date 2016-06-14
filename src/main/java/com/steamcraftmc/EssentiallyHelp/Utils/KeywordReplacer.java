package com.steamcraftmc.EssentiallyHelp.Utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class KeywordReplacer implements IText {
    private static final Pattern KEYWORD = Pattern.compile("\\{([^\\{\\}]+)\\}");
    private static final Pattern KEYWORDSPLIT = Pattern.compile("\\:");
    private final transient IText input;
    private final transient List<String> replaced;
    private final transient boolean includePrivate;
    private final EnumMap<KeywordType, Object> keywordCache = new EnumMap<KeywordType, Object>(KeywordType.class);

    public KeywordReplacer(final IText input, final Player sender) {
        this.input = input;
        this.replaced = new ArrayList<String>(this.input.getLines().size());
        this.includePrivate = true;
        replaceKeywords(sender);
    }

    private void replaceKeywords(final Player user) {

        for (int i = 0; i < input.getLines().size(); i++) {
            String line = input.getLines().get(i);
            final Matcher matcher = KEYWORD.matcher(line);

            while (matcher.find()) {
                final String fullMatch = matcher.group(0);
                final String keywordMatch = matcher.group(1);
                final String[] matchTokens = KEYWORDSPLIT.split(keywordMatch);
                line = replaceLine(line, fullMatch, matchTokens, user);
            }
            replaced.add(line);
        }
    }

    @SuppressWarnings("unchecked")
	private String replaceLine(String line, final String fullMatch, final String[] matchTokens, final Player user) {
        final String keyword = matchTokens[0];
        try {
            String replacer = null;
            KeywordType validKeyword = KeywordType.valueOf(keyword);
            if (validKeyword.getType().equals(KeywordCachable.CACHEABLE) && keywordCache.containsKey(validKeyword)) {
                replacer = keywordCache.get(validKeyword).toString();
            } else if (validKeyword.getType().equals(KeywordCachable.SUBVALUE)) {
                String subKeyword = "";
                if (matchTokens.length > 1) {
                    subKeyword = matchTokens[1].toLowerCase(Locale.ENGLISH);
                }

                if (keywordCache.containsKey(validKeyword)) {
                    Map<String, String> values = (Map<String, String>) keywordCache.get(validKeyword);
                    if (values.containsKey(subKeyword)) {
                        replacer = values.get(subKeyword);
                    }
                }
            }

            if (validKeyword.isPrivate() && !includePrivate) {
                replacer = "";
            }

            if (replacer == null) {
                replacer = "";
                switch (validKeyword) {
                    case PLAYER:
                    case DISPLAYNAME:
                        if (user != null) {
                            replacer = user.getDisplayName();
                        }
                        break;
                    case USERNAME:
                        if (user != null) {
                            replacer = user.getName();
                        }
                        break;
                    //case BALANCE:
                    //    if (user != null) {
                    //        replacer = NumberUtil.displayCurrency(user.getMoney(), ess);
                    //    }
                    //    break;
                    case WORLD:
                    case WORLDNAME:
                        if (user != null) {
                            final Location location = user.getLocation();
                            replacer = location == null || location.getWorld() == null ? "" : location.getWorld().getName();
                        }
                        break;
                    case ONLINE:
                    case UNIQUE:
                        replacer = Integer.toString(user.getWorld().getPlayers().size());
                        break;
                    case TIME:
                        replacer = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
                        break;
                    case DATE:
                        replacer = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
                        break;
                    case WORLDTIME12:
                        if (user != null) {
                            replacer = DescParseTickFormat.format12(user.getWorld() == null ? 0 : user.getWorld().getTime());
                        }
                        break;
                    case WORLDTIME24:
                        if (user != null) {
                            replacer = DescParseTickFormat.format24(user.getWorld() == null ? 0 : user.getWorld().getTime());
                        }
                        break;
                    case WORLDDATE:
                        if (user != null) {
                            replacer = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                            		.format(DescParseTickFormat.ticksToDate(user.getWorld() == null ? 0 : user.getWorld().getFullTime()));
                        }
                        break;
                    case COORDS:
                        if (user != null) {
                            final Location location = user.getLocation();
                            replacer = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
                        }
                        break;
                    default:
                        replacer = "N/A";
                        break;
                }

                //If this is just a regular keyword, lets throw it into the cache
                if (validKeyword.getType().equals(KeywordCachable.CACHEABLE)) {
                    keywordCache.put(validKeyword, replacer);
                }
            }

            line = line.replace(fullMatch, replacer);
        } catch (IllegalArgumentException ex) {
        }

        return line;
    }

    @Override
    public List<String> getLines() {
        return replaced;
    }

    @Override
    public List<String> getChapters() {
        return input.getChapters();
    }

    @Override
    public Map<String, Integer> getBookmarks() {
        return input.getBookmarks();
    }
}

//When adding a keyword here, you also need to add the implementation above
enum KeywordType {
    PLAYER(KeywordCachable.CACHEABLE),
    DISPLAYNAME(KeywordCachable.CACHEABLE),
    USERNAME(KeywordCachable.NOTCACHEABLE),
    BALANCE(KeywordCachable.CACHEABLE),
    MAILS(KeywordCachable.CACHEABLE),
    WORLD(KeywordCachable.CACHEABLE),
    WORLDNAME(KeywordCachable.CACHEABLE),
    ONLINE(KeywordCachable.CACHEABLE),
    UNIQUE(KeywordCachable.CACHEABLE),
    WORLDS(KeywordCachable.CACHEABLE),
    PLAYERLIST(KeywordCachable.SUBVALUE, true),
    TIME(KeywordCachable.CACHEABLE),
    DATE(KeywordCachable.CACHEABLE),
    WORLDTIME12(KeywordCachable.CACHEABLE),
    WORLDTIME24(KeywordCachable.CACHEABLE),
    WORLDDATE(KeywordCachable.CACHEABLE),
    COORDS(KeywordCachable.CACHEABLE),
    TPS(KeywordCachable.CACHEABLE),
    UPTIME(KeywordCachable.CACHEABLE),
    IP(KeywordCachable.CACHEABLE, true),
    ADDRESS(KeywordCachable.CACHEABLE, true),
    PLUGINS(KeywordCachable.CACHEABLE, true),
    VERSION(KeywordCachable.CACHEABLE, true);
    private final KeywordCachable type;
    private final boolean isPrivate;

    KeywordType(KeywordCachable type) {
        this.type = type;
        this.isPrivate = false;
    }

    KeywordType(KeywordCachable type, boolean isPrivate) {
        this.type = type;
        this.isPrivate = isPrivate;
    }

    public KeywordCachable getType() {
        return type;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}


enum KeywordCachable {
    CACHEABLE, // This keyword can be cached as a string
    SUBVALUE, // This keyword can be cached as a map
    NOTCACHEABLE // This keyword should never be cached
}