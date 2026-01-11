package org.partypanelplus.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;

@RequiredArgsConstructor
@Getter
@Setter
public class PartyTilePingData
{
    private final WorldPoint point;
    private final Color color;
    private int ticksRemaining = 1;
}
