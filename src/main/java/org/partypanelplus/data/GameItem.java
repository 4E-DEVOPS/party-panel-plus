package org.partypanelplus.data;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.QuantityFormatter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Value
@AllArgsConstructor
public class GameItem
{
	int id;
	int qty;
	String name;
	boolean stackable;
	int price;

	public GameItem(final Item item, final ItemManager itemManager)
	{
		this(item.getId(), item.getQuantity(), itemManager);
	}

	public GameItem(final int id, final int qty, final ItemManager itemManager)
	{
		this.id = id;
		this.qty = qty;

		final ItemComposition c = itemManager.getItemComposition(id);

		this.name = c.getName();
		this.stackable = c.isStackable();

		// Use linked note ID if noted item
		int priceId = c.getNote() != -1 ? c.getLinkedNoteId() : id;
		this.price = itemManager.getItemPrice(priceId);
	}

	public static GameItem[] convertItemsToGameItems(final int[] items, final ItemManager itemManager)
	{
		GameItem[] output = new GameItem[items.length / 2];
		for (int i = 0; i < items.length; i += 2)
		{
			if (items[i] == -1 || items[i + 1] <= 0)
			{
				output[i / 2] = null;
			}
			else
			{
				output[i / 2] = new GameItem(items[i], items[i + 1], itemManager);
			}
		}
		return output;
	}

	public static GameItem[] convertItemsToGameItems(final Item[] items, final ItemManager itemManager)
	{
		final GameItem[] output = new GameItem[items.length];
		for (int i = 0; i < items.length; i++)
		{
			final Item item = items[i];
			if (item == null || item.getId() == -1)
			{
				output[i] = null;
			}
			else
			{
				output[i] = new GameItem(item, itemManager);
			}
		}
		return output;
	}

	public String getDisplayName()
	{
		if (this.qty <= 1)
		{
			return this.name;
		}

		return this.name + " x " + QuantityFormatter.formatNumber(this.qty);
	}

	public int getTotalPrice()
	{
		return price * qty;
	}

	public String getFormattedTotalPrice()
	{
		return QuantityFormatter.formatNumber(getTotalPrice()) + " gp";
	}

	public boolean isEmpty()
	{
		return id == -1 || qty <= 0;
	}

	public Map<String, Object> toMap()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("qty", qty);
		map.put("name", name);
		map.put("stackable", stackable);
		map.put("price", price);
		return map;
	}

	public static List<Map<String, Object>> toMapList(GameItem[] items)
	{
		List<Map<String, Object>> list = new ArrayList<>();
		for (GameItem item : items)
		{
			if (item != null)
			{
				list.add(item.toMap());
			}
		}
		return list;
	}

	public static GameItem fromMap(Map<String, Object> map)
	{
		int id = ((Number) map.get("id")).intValue();
		int qty = ((Number) map.get("qty")).intValue();

		String name = (String) map.getOrDefault("name", null);
		boolean stack = map.containsKey("stackable") && (Boolean) map.get("stackable");
		int price = map.containsKey("price") ? ((Number) map.get("price")).intValue() : 0;

		return new GameItem(id, qty, name, stack, price);
	}

	public static GameItem[] fromMapList(List<Map<String, Object>> list)
	{
		GameItem[] items = new GameItem[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			items[i] = fromMap(list.get(i));
		}
		return items;
	}
}
