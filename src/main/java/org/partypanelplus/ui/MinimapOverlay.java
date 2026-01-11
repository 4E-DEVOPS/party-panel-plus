package org.partypanelplus.ui;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
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
        setLayer(OverlayLayer.ABOVE_MAP);
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
            if (partyPlayer.isLocal(client) || partyPlayer.getWorld() != client.getWorld())
                continue;

            Player player = partyPlayer.getPlayer();
            if (player == null)
                continue;

            LocalPoint lp = player.getLocalLocation();
            if (lp == null)
                continue;

            // Orange by default
            Color dotColor = Color.ORANGE;

            graphics.setColor(dotColor);
            graphics.fillOval(
                    lp.getSceneX() / 32 + 3, // X offset tweak
                    lp.getSceneY() / 32 + 3, // Y offset tweak
                    4, 4 // Dot size
            );
        }

        return null;
    }
}
