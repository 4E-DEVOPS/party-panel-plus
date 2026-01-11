package org.partypanelplus.data;

import java.awt.Color;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.party.PartyMember;
import org.partypanelplus.PartyPlusPlugin;

@Data
@EqualsAndHashCode
public class PartyPlayer
{
	private transient PartyMember member;
	private String username;
	private Stats stats;
	private GameItem[] inventory;
	private GameItem[] equipment;
	private Prayers prayers;
	private int stamina;
	private int world;
	private GameItem[] runesInPouch;
	private Quiver quiver;
	private Color playerColor;
	private WorldPoint location;

	public PartyPlayer(final PartyMember member)
	{
		this.member = member;
		this.username = "";
		this.stats = null;
		this.inventory = new GameItem[28];
		this.equipment = new GameItem[EquipmentInventorySlot.AMMO.getSlotIdx() + 1];
		this.prayers = null;
		this.stamina = 0;
		this.world = 0;
		this.runesInPouch = new GameItem[0];
		this.quiver = new Quiver(null, false, false);
		this.playerColor = null;
		this.location = null;
	}

	public PartyPlayer(final PartyMember member, final Client client, final ItemManager itemManager, final ClientThread clientThread)
	{
		this(member);
		this.stamina = client.getVarbitValue(Varbits.STAMINA_EFFECT);
		this.world = client.getWorld();
		clientThread.invoke(() -> updatePlayerInfo(client, itemManager));
	}

	public void updatePlayerInfo(final Client client, final ItemManager itemManager)
	{
		assert client.isClientThread();

		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			this.username = localPlayer.getName();
			this.stats = new Stats(client);
			this.location = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation());

			final ItemContainer invi = client.getItemContainer(InventoryID.INVENTORY);
			if (invi != null)
			{
				this.inventory = GameItem.convertItemsToGameItems(invi.getItems(), itemManager);
				final List<Item> runesInPouch = PartyPlusPlugin.getRunePouchContents(client);
				this.runesInPouch = GameItem.convertItemsToGameItems(runesInPouch.toArray(new Item[0]), itemManager);

				boolean hasQuiverInInventory = false;
				for (final Item item : invi.getItems())
				{
					if (PartyPlusPlugin.DIZANAS_QUIVER_IDS.contains(item.getId()))
					{
						hasQuiverInInventory = true;
						break;
					}
				}
				quiver.setInInventory(hasQuiverInInventory);
			}

			final ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
			if (equip != null)
			{
				this.equipment = GameItem.convertItemsToGameItems(equip.getItems(), itemManager);

				final Item cape = equip.getItem(EquipmentInventorySlot.CAPE.getSlotIdx());
				boolean isWearingQuiver = cape != null && PartyPlusPlugin.DIZANAS_QUIVER_IDS.contains(cape.getId());
				quiver.setBeingWorn(isWearingQuiver);
			}

			if (this.prayers == null)
			{
				prayers = new Prayers(client);
			}
		}

		if (quiver.isSlotVisible())
		{
			final int quiverAmmoId = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_ID);
			final int quiverAmmoCount = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_COUNT);
			if (quiverAmmoId == -1 || quiverAmmoCount == 0)
			{
				quiver.setQuiverAmmo(null);
			}
			else
			{
				quiver.setQuiverAmmo(new GameItem(quiverAmmoId, quiverAmmoCount, itemManager));
			}
		}
	}

	/** ==============================
	 *  LEVEL GETTERS
	 * ============================== */

	public int getSkillBoostedLevel(final Skill skill)
	{
		return stats == null ? 0 : stats.getBoostedLevels().get(skill);
	}

	public int getSkillRealLevel(final Skill skill)
	{
		return getSkillRealLevel(skill, false);
	}

	public int getSkillRealLevel(final Skill skill, final boolean allowVirtualLevels)
	{
		if (stats == null)
		{
			return 0;
		}

		return Math.min(stats.getBaseLevels().get(skill), allowVirtualLevels ? 126 : 99);
	}

	/** ==============================
	 *  STATUS HELPERS (for overlays)
	 * ============================== */

	public boolean isFrozen() { return stats != null && stats.isFrozen(); }
	public boolean isBurning() { return stats != null && stats.isBurning(); }
	public boolean isAfk() { return stats != null && stats.isAfk(); }

	public int getPoison() { return stats != null ? stats.getPoison() : 0; }
	public int getDisease() { return stats != null ? stats.getDisease() : 0; }

	/** ==============================
	 *  Location helpers
	 * ============================== */

	public WorldPoint getLocation()
	{
		return location;
	}

	public void setLocation(WorldPoint location)
	{
		this.location = location;
	}

	/** ==============================
	 *  Local player comparison
	 * ============================== */

	public boolean isLocal(Client client)
	{
		return client.getLocalPlayer() != null
				&& member != null
				&& client.getLocalPlayer().getName().equalsIgnoreCase(username);
	}
}
