package org.partypanelplus;

import net.runelite.client.config.ConfigManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PartyHistoryStore
{
    private static final int MAX_HISTORY = 10;
    private static final String GROUP = "partypanelplus";
    private static final String HISTORY_KEY = "partyNameHistory";

    public static void appendToHistory(ConfigManager config, String name)
    {
        if (name == null || (name = name.trim()).isEmpty())
        {
            return;
        }

        if (name.contains(","))
        {
            // Prevent corrupting CSV structure
            return;
        }

        List<String> history = new LinkedList<>();
        String stored = config.getConfiguration(GROUP, HISTORY_KEY);
        if (stored != null && !stored.isEmpty())
        {
            for (String entry : stored.split(","))
            {
                if (!entry.equalsIgnoreCase(name))
                {
                    history.add(entry);
                }
            }
        }

        history.add(0, name);

        while (history.size() > MAX_HISTORY)
        {
            history.remove(history.size() - 1);
        }

        String joined = String.join(",", history);
        config.setConfiguration(GROUP, HISTORY_KEY, joined);
    }

    public static List<String> getHistory(ConfigManager config)
    {
        String stored = config.getConfiguration(GROUP, HISTORY_KEY);
        if (stored == null || stored.isEmpty())
        {
            return new ArrayList<>();
        }

        String[] split = stored.split(",");
        List<String> history = new ArrayList<>(split.length);
        for (String s : split)
        {
            if (!s.isEmpty())
            {
                history.add(s);
            }
        }
        return history;
    }

    public static void clearHistory(ConfigManager config)
    {
        config.unsetConfiguration(GROUP, HISTORY_KEY);
    }

    public static void removeFromHistory(ConfigManager config, String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            return;
        }

        List<String> history = getHistory(config);
        history.removeIf(entry -> entry.equalsIgnoreCase(name.trim()));

        String joined = String.join(",", history);
        config.setConfiguration(GROUP, HISTORY_KEY, joined);
    }
}
