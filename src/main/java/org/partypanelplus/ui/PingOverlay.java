package org.partypanelplus.ui;

import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyTilePingData;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PingOverlay extends Overlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;

    private final List<PartyTilePingData> activePings = new ArrayList<>();

    @Inject
    public PingOverlay(Client client, PartyPlusPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Avoid ConcurrentModificationException
        for (PartyTilePingData ping : new ArrayList<>(activePings))
        {
            WorldPoint wp = ping.getPoint();

            // Only show on current plane
            if (wp.getPlane() != client.getPlane())
            {
                continue;
            }

            LocalPoint local = LocalPoint.fromWorld(client, wp);
            if (local == null)
            {
                continue;
            }

            int sceneX = local.getSceneX() >> 7;
            int sceneY = local.getSceneY() >> 7;

            Tile[][][] tiles = client.getScene().getTiles();
            if (sceneX < 0 || sceneY < 0 || sceneX >= tiles[0].length || sceneY >= tiles[0][sceneX].length)
            {
                continue;
            }

            Tile tile = tiles[client.getPlane()][sceneX][sceneY];
            if (tile == null)
            {
                continue;
            }

            Color color = ping.getColor();

            // ✅ Draw tile polygon outline using Perspective
            Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, color);
            }

            // ✅ Optional: Draw vertical beam above tile
            net.runelite.api.Point rlPoint = Perspective.localToCanvas(client, tile.getLocalLocation(), client.getPlane());
            if (rlPoint != null)
            {
                int x = rlPoint.getX();
                int y = rlPoint.getY();

                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
                graphics.fillRect(x - 2, y - 60, 4, 60);
            }
        }

        return null;
    }

    public void addPing(WorldPoint point, Color color)
    {
        activePings.add(new PartyTilePingData(point, color));
    }

    public void removeExpiredPings()
    {
        Iterator<PartyTilePingData> it = activePings.iterator();
        while (it.hasNext())
        {
            PartyTilePingData ping = it.next();
            ping.setTicksRemaining(ping.getTicksRemaining() - 1);
            if (ping.getTicksRemaining() <= 0)
            {
                it.remove();
            }
        }
    }
}
