package org.partypanelplus.ui;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import org.partypanelplus.PartyPlusConfig;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyPlayer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class MapOverlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;
    private final PartyPlusConfig config;
    private final WorldMapOverlay worldMapOverlay;

    private final Map<Long, WorldMapPoint> mapPoints = new HashMap<>();

    @Inject
    public MapOverlay(
            Client client,
            PartyPlusPlugin plugin,
            PartyPlusConfig config,
            WorldMapOverlay worldMapOverlay
    )
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.worldMapOverlay = worldMapOverlay;
    }

    public void updateMapPoints()
    {
        if (!plugin.isInParty())
            return;

        // Clear existing points
        for (WorldMapPoint point : mapPoints.values())
        {
            worldMapOverlay.remove(point);
        }
        mapPoints.clear();

        for (PartyPlayer player : plugin.getPartyMembers().values())
        {
            if (player.isLocal(client))
                continue;

            if (config.incognitoMode())
                continue;

            WorldPoint location = player.getLocation();
            if (location == null)
                continue;

            BufferedImage icon = generateInitialIcon(player);
            if (icon == null)
                continue;

            WorldMapPoint point = new WorldMapPoint(location, icon);
            point.setTooltip(buildTooltip(player));
            point.setJumpOnClick(false);
            point.setName(player.getUsername());

            mapPoints.put(player.getMember().getMemberId(), point);
            worldMapOverlay.add(point);
        }
    }

    private String buildTooltip(PartyPlayer player)
    {
        String name = player.getUsername() != null ? player.getUsername() : "Unknown";
        return name + " (W" + player.getWorld() + ")";
    }

    /**
     * Generate a party icon for the world map:
     * - If Discord avatar is available, use it.
     * - Otherwise, fall back to a colored initial.
     */
    private BufferedImage generateInitialIcon(PartyPlayer player)
    {
        if (player.getMember() != null && player.getMember().getAvatar() != null)
        {
            return player.getMember().getAvatar();
        }

        String name = player.getUsername();
        if (name == null || name.isEmpty())
            return null;

        char firstChar = Character.toUpperCase(name.charAt(0));
        Color color = player.getPlayerColor() != null ? player.getPlayerColor() : Color.ORANGE;

        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background circle
        g.setColor(color);
        g.fillOval(0, 0, size, size);

        // Text
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.charWidth(firstChar);
        int textHeight = fm.getAscent();

        g.setColor(Color.BLACK);
        g.drawString(
                String.valueOf(firstChar),
                (size - textWidth) / 2,
                (size + textHeight) / 2 - 2
        );

        g.dispose();
        return image;
    }
}
