package org.partypanelplus.ui;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.plugins.party.messages.TilePing;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.partypanelplus.PartyPlusPlugin;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PingOverlay extends Overlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;

    @Getter
    private final List<PartyPing> partyPings = new CopyOnWriteArrayList<>();
    private final List<LootBeam> lootBeams = new CopyOnWriteArrayList<>();

    private static final int BEAM_HEIGHT = 60;
    private static final int DEFAULT_PING_TICKS = 6; // ~3.6s @20tps

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
        if (client.getLocalPlayer() == null)
            return null;

        final int plane = client.getPlane();

        // === PARTY TILE PINGS ===
        for (PartyPing pp : partyPings)
        {
            TilePing ping = pp.ping;
            WorldPoint wp = ping.getPoint();

            if (wp.getPlane() != plane)
                continue;

            LocalPoint lp = LocalPoint.fromWorld(client, wp);
            if (lp == null)
                continue;

            Tile tile = client.getScene().getTiles()[plane][lp.getSceneX()][lp.getSceneY()];
            if (tile == null)
                continue;

            Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, pp.color);
            }

            Point canvas = Perspective.localToCanvas(client, lp, plane);
            if (canvas != null)
            {
                drawBeam(graphics, canvas, pp.color);
            }
        }

        // === LOOT BEAMS ===
        for (LootBeam beam : lootBeams)
        {
            WorldPoint wp = beam.location;
            if (wp.getPlane() != plane)
                continue;

            LocalPoint lp = LocalPoint.fromWorld(client, wp);
            if (lp == null)
                continue;

            Point canvas = Perspective.localToCanvas(client, lp, plane);
            if (canvas != null)
            {
                drawBeam(graphics, canvas, beam.color);
            }
        }

        return null;
    }

    private void drawBeam(Graphics2D g, Point p, Color color)
    {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.fillRect(p.getX() - 2, p.getY() - BEAM_HEIGHT, 4, BEAM_HEIGHT);
    }

    // === PUBLIC API ===

    public void addPartyPing(TilePing ping)
    {
        Color color = plugin.getColorForMember(ping.getMemberId());
        if (color == null)
            color = Color.CYAN;

        partyPings.add(new PartyPing(ping, color, DEFAULT_PING_TICKS));
    }

    public void addPartyPing(WorldPoint wp, Color color, int ticks)
    {
        TilePing ping = new TilePing(wp);
        partyPings.add(new PartyPing(ping, color, ticks));
    }

    public void addLootBeam(WorldPoint wp, Color color, int ticks)
    {
        lootBeams.add(new LootBeam(wp, color, ticks));
    }

    public void tickDown()
    {
        // party pings
        Iterator<PartyPing> it = partyPings.iterator();
        while (it.hasNext())
        {
            PartyPing pp = it.next();
            if (--pp.ticks <= 0)
                it.remove();
        }

        // loot beams
        Iterator<LootBeam> lb = lootBeams.iterator();
        while (lb.hasNext())
        {
            LootBeam beam = lb.next();
            if (--beam.ticks <= 0)
                lb.remove();
        }
    }

    // === INNER CLASSES ===

    private static class PartyPing
    {
        final TilePing ping;
        final Color color;
        int ticks;

        PartyPing(TilePing ping, Color color, int ticks)
        {
            this.ping = ping;
            this.color = color;
            this.ticks = ticks;
        }
    }

    private static class LootBeam
    {
        final WorldPoint location;
        final Color color;
        int ticks;

        LootBeam(WorldPoint location, Color color, int ticks)
        {
            this.location = location;
            this.color = color;
            this.ticks = ticks;
        }
    }
}
