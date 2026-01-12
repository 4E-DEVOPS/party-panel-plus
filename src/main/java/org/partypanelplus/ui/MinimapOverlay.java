package org.partypanelplus.ui;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.partypanelplus.PartyPlusConfig;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyPlayer;

import java.awt.*;

public class MinimapOverlay extends Overlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;
    private final PartyPlusConfig config;

    @Inject
    public MinimapOverlay(Client client, PartyPlusPlugin plugin, PartyPlusConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInParty() || client.getLocalPlayer() == null)
        {
            return null;
        }

        for (PartyPlayer partyPlayer : plugin.getPartyMembers().values())
        {
            if (partyPlayer.isLocal(client))
                continue;

            Player p = partyPlayer.getPlayer();
            if (p == null)
                continue;

            boolean isCrossWorld = partyPlayer.getWorld() != client.getWorld();
            if (isCrossWorld && !config.showPlayersAcrossWorlds())
                continue;

            LocalPoint lp = p.getLocalLocation();
            if (lp == null)
                continue;

            Point minimapPoint = Perspective.getCanvasTextMiniMapLocation(client, graphics, lp, "");
            if (minimapPoint == null)
                continue;

            // === Dot Color Logic ===
            Color dotColor = isCrossWorld
                    ? config.crossWorldDotColor()
                    : config.usePerPlayerDotColors()
                    ? partyPlayer.getPlayerColor()
                    : config.partyDotColor();

            graphics.setColor(dotColor);
            graphics.fillOval(minimapPoint.getX(), minimapPoint.getY(), 4, 4);
        }

        return null;
    }
}
