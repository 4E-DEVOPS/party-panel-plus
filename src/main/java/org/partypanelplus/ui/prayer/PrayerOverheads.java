package org.partypanelplus.ui.prayer;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.partypanelplus.PartyPlusConfig;
import org.partypanelplus.PartyPlusPlugin;
import org.partypanelplus.data.PartyPlayer;
import org.partypanelplus.data.PrayerData;
import org.partypanelplus.data.Prayers;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PrayerOverheads extends Overlay
{
    private final Client client;
    private final PartyPlusPlugin plugin;
    private final PartyPlusConfig config;

    @Inject
    public PrayerOverheads(Client client, PartyPlusPlugin plugin, PartyPlusConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        if (!plugin.isInParty() || !config.showOthersOverheads())
        {
            return null;
        }

        for (PartyPlayer partyPlayer : plugin.getPartyMembers().values())
        {
            if (partyPlayer.isLocal(client))
                continue;

            Player renderTarget = partyPlayer.getPlayer();
            if (renderTarget == null)
                continue;

            Prayers prayers = partyPlayer.getPrayers();
            if (prayers == null)
                continue;

            for (PrayerSprites p : PrayerSprites.values())
            {
                PrayerData data = prayers.getPrayerData().get(p.getPrayer());

                if (data != null && data.isEnabled() && isProtectionPrayer(p.getPrayer()))
                {
                    BufferedImage sprite = p.getSprite();
                    if (sprite != null)
                    {
                        Point drawLoc = renderTarget.getCanvasImageLocation(sprite, renderTarget.getLogicalHeight() + 30);
                        if (drawLoc != null)
                        {
                            OverlayUtil.renderImageLocation(g, drawLoc, sprite);
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isProtectionPrayer(Prayer prayer)
    {
        return prayer == Prayer.PROTECT_FROM_MELEE
                || prayer == Prayer.PROTECT_FROM_MISSILES
                || prayer == Prayer.PROTECT_FROM_MAGIC
                || prayer == Prayer.DEFLECT_MELEE
                || prayer == Prayer.DEFLECT_MISSILES
                || prayer == Prayer.DEFLECT_MAGIC;
    }
}
