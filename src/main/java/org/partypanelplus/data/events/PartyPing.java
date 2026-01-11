package org.partypanelplus.data.events;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.party.messages.PartyMessage;

@Getter
@Setter
public class PartyPing extends PartyMessage
{
    private int x;
    private int y;
    private int plane;
    private long memberId;

    public PartyPing(WorldPoint point, long memberId)
    {
        this.x = point.getX();
        this.y = point.getY();
        this.plane = point.getPlane();
        this.memberId = memberId;
    }

    public WorldPoint toWorldPoint()
    {
        return new WorldPoint(x, y, plane);
    }
}
