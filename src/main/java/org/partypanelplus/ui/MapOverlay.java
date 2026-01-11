package org.partypanelplus.ui;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import org.partypanelplus.PartyPlusConfig;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyPlayer;
import org.partypanelplus.ImgUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class MapOverlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;
    private final PartyPlusConfig config;
    private final WorldMapPointManager mapPointManager;

    private final Map<Long, WorldMapPoint> mapPoints = new HashMap<>();

    @Inject
    public MapOverlay(
            Client client,
            PartyPlusPlugin plugin,
            PartyPlusConfig config,
            WorldMapPointManager mapPointManager
    )
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.mapPointManager = mapPointManager;
    }

    public void updateMapPoints()
    {
        if (!plugin.isInParty())
            return;

        // Clear old map points
        for (WorldMapPoint point : mapPoints.values())
        {
            mapPointManager.remove(point);
        }
        mapPoints.clear();

        // Add new map points for each remote party player
        for (PartyPlayer player : plugin.getPartyMembers().values())
        {
            if (player.isLocal(client))
                continue;

            if (config.incognitoMode())
                continue;

            boolean isCrossWorld = player.getWorld() != client.getWorld();
            if (isCrossWorld && !config.showPlayersAcrossWorlds())
                continue;

            WorldPoint location = player.getLocation();
            if (location == null)
                continue;

            BufferedImage icon = generateInitialIcon(player);
            if (icon == null)
                continue;

            // Apply grayscale if player is in another world
            if (isCrossWorld)
            {
                icon = ImgUtil.makeGrayscale(icon);
            }

            WorldMapPoint point = new WorldMapPoint(location, icon);
            point.setTooltip(buildTooltip(player));
            point.setJumpOnClick(false);
            point.setName(player.getUsername());

            mapPoints.put(player.getMember().getMemberId(), point);
            mapPointManager.add(point);
        }
    }

    private String buildTooltip(PartyPlayer player)
    {
        String name = player.getUsername() != null ? player.getUsername() : "Unknown";
        WorldPoint location = player.getLocation();

        if (location == null)
        {
            return name + "\nWorld: " + player.getWorld();
        }

        return String.format(
                "%s\nX: %d, Y: %d, Plane: %d\nWorld: %d",
                name,
                location.getX(),
                location.getY(),
                location.getPlane(),
                player.getWorld()
        );
    }

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

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Circle background
        g.setColor(color);
        g.fillOval(0, 0, size, size);

        // Initial letter
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
