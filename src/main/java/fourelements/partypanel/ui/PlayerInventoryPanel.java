package fourelements.partypanel.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.apache.commons.lang3.ArrayUtils;

import fourelements.partypanel.PartyPanelPlugin;
import fourelements.partypanel.data.GameItem;

public class PlayerInventoryPanel extends JPanel
{
	private static final int INVENTORY_COLUMNS = 4;
	private static final int INVENTORY_ROWS = 7;
	private static final int INVENTORY_SIZE = 28;

	private static final int H_GAP = 2;
	private static final int V_GAP = 2;

	private static final Dimension SLOT_SIZE = new Dimension(50, 42);
	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);
	private static final Color BACKGROUND_COLOR = new Color(62, 53, 41);

	private final ItemManager itemManager;

	public PlayerInventoryPanel(final GameItem[] items, final GameItem[] runePouchContents, final ItemManager itemManager)
	{
		this.itemManager = itemManager;

		setLayout(new DynamicGridLayout(INVENTORY_ROWS, INVENTORY_COLUMNS, H_GAP, V_GAP));
		setBackground(BACKGROUND_COLOR);
		setPreferredSize(PANEL_SIZE);

		updateInventory(items, runePouchContents);
	}

	public void updateInventory(final GameItem[] items, final GameItem[] runePouchContents)
	{
		this.removeAll();

		// Add inventory slots
		for (final GameItem item : items)
		{
			final JLabel label = createInventoryLabel();

			if (item != null)
			{
				String tooltip = ArrayUtils.contains(PartyPanelPlugin.RUNEPOUCH_ITEM_IDS, item.getId())
						? getRunePouchHoverText(item, runePouchContents)
						: item.getDisplayName();

				label.setToolTipText(tooltip);
				itemManager.getImage(item.getId(), item.getQty(), item.isStackable()).addTo(label);
			}

			add(label);
		}

		// Fill remaining slots (in case of less than 28 items)
		for (int i = getComponentCount(); i < INVENTORY_SIZE; i++)
		{
			add(createInventoryLabel());
		}

		revalidate();
		repaint();
	}

	private JLabel createInventoryLabel()
	{
		JLabel label = new JLabel();
		label.setPreferredSize(SLOT_SIZE);
		label.setMinimumSize(SLOT_SIZE);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setVerticalAlignment(JLabel.CENTER);
		return label;
	}

	public String getRunePouchHoverText(final GameItem runePouch, final GameItem[] contents)
	{
		String contentNames = Arrays.stream(contents)
				.filter(Objects::nonNull)
				.map(GameItem::getDisplayName)
				.collect(Collectors.joining("<br>"));

		if (contentNames.isEmpty())
		{
			return runePouch.getDisplayName();
		}

		return "<html>" + runePouch.getDisplayName() + "<br><br>" + contentNames + "</html>";
	}
}
