package org.partypanelplus.ui;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import org.partypanelplus.PartyPlusConfig;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyPlayer;
import org.partypanelplus.data.Stats;

import java.awt.*;

public class PlayerOverlays extends Overlay
{
    private final PartyPlusPlugin plugin;
    private final Client client;
    private final PartyPlusConfig config;
    private final ModelOutlineRenderer outlineRenderer;

    @Inject
    public PlayerOverlays(Client client, PartyPlusPlugin plugin, PartyPlusConfig config, ModelOutlineRenderer outlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.outlineRenderer = outlineRenderer;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInParty() || client.getLocalPlayer() == null)
            return null;

        for (PartyPlayer pp : plugin.getPartyMembers().values())
        {
            // ignore local
            if (pp.isLocal(client)) continue;

            // world check
            if (!config.showPlayersAcrossWorlds() && pp.getWorld() != client.getWorld()) continue;

            // find corresponding OSRS player by username
            Player p = client.getPlayers().stream()
                    .filter(pl -> pl.getName() != null && pl.getName().equalsIgnoreCase(pp.getUsername()))
                    .findFirst()
                    .orElse(null);

            if (p == null) continue;

            switch (config.overlayMode())
            {
                case NAMEPLATES:
                    renderNameplate(graphics, pp, p);
                    break;

                case HIGHLIGHT:
                    renderHighlight(pp, p);
                    break;

                case STATUS:
                    renderStatus(pp, p);
                    break;
            }
        }

        return null;
    }

    private void renderNameplate(Graphics2D graphics, PartyPlayer pp, Player p)
    {
        String name = pp.getUsername() != null ? pp.getUsername() : p.getName();
        if (name == null) return;

        // fix cross-type issue
        net.runelite.api.Point apiLoc =
                p.getCanvasTextLocation(graphics, name, p.getLogicalHeight() + 40);

        if (apiLoc == null) return;

        java.awt.Point loc = new java.awt.Point(apiLoc.getX(), apiLoc.getY());

        Font base = config.nameplateBold()
                ? FontManager.getRunescapeBoldFont()
                : FontManager.getRunescapeFont();

        graphics.setFont(base.deriveFont((float) config.nameplateSize()));

        TextComponent text = new TextComponent();
        text.setText(name);
        text.setColor(config.nameplateColor());
        text.setPosition(loc);
        text.render(graphics);
    }

    private void renderHighlight(PartyPlayer pp, Player p)
    {
        Color c = pp.getPlayerColor();
        if (c == null) c = new Color(0, 255, 255); // Bright yellow

        outlineRenderer.drawOutline(p, 2, c, 300);
    }

    private void renderStatus(PartyPlayer pp, Player p)
    {
        Stats stats = pp.getStats();
        if (stats == null) return;

        Color state = null;

        if (stats.getPoison() > 0) state = Color.GREEN;
        else if (stats.getDisease() > 0) state = Color.YELLOW;
        else if (pp.isFrozen()) state = Color.CYAN;
        else if (pp.isBurning()) state = Color.RED;
        else if (pp.isAfk()) state = Color.GRAY;

        if (state != null)
        {
            outlineRenderer.drawOutline(p, 2, state, 300);
        }
    }
}
