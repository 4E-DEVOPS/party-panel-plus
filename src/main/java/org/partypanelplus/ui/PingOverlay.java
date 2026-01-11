package org.partypanelplus.ui;

import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
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
        for (PartyTilePingData ping : new ArrayList<>(activePings))
        {
            if (ping.getPoint().getPlane() != client.getPlane())
                continue;

            LocalPoint local = LocalPoint.fromWorld(client, ping.getPoint());
            if (local == null)
                continue;

            Tile tile = client.getScene().getTiles()[client.getPlane()]
                    [local.getSceneX() / 128][local.getSceneY() / 128];

            if (tile == null)
                continue;

            Color color = ping.getColor();

            // Draw tile highlight
            OverlayUtil.renderTileOverlay(graphics, tile, color, null);

            // Optional: Draw light beam effect
            Point canvasPoint = tile.getCanvasLocation(0);
            if (canvasPoint != null)
            {
                graphics.setColor(color);
                graphics.fillRect(canvasPoint.getX() - 2, canvasPoint.getY() - 40, 4, 40);
            }
        }

        return null;
    }

    /**
     * Adds a new ping at the specified location and color.
     */
    public void addPing(WorldPoint point, Color color)
    {
        activePings.add(new PartyTilePingData(point, color));
    }

    /**
     * Call this once per game tick to decrement and remove expired pings.
     */
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
