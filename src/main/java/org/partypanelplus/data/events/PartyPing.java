package org.partypanelplus.data.events;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.party.messages.PartyMessage;

@Getter
@Setter
public class PartyPing extends PartyMessage
{
    private int x, y, plane;
    private long memberId;

    public PartyPing(WorldPoint wp, long memberId)
    {
        this.x = wp.getX();
        this.y = wp.getY();
        this.plane = wp.getPlane();
        this.memberId = memberId;
    }

    public WorldPoint toWorldPoint()
    {
        return new WorldPoint(x, y, plane);
    }
}
