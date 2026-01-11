package org.partypanelplus.ui.equipment;

import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import org.partypanelplus.data.GameItem;
import org.partypanelplus.data.Quiver;

public class PlayerEquipmentPanel extends JPanel
{
	private static final ImmutableMap<EquipmentInventorySlot, Integer> EQUIPMENT_SLOT_SPRITE_MAP;

	static
	{
		final ImmutableMap.Builder<EquipmentInventorySlot, Integer> sprites = new ImmutableMap.Builder<>();
		sprites.put(EquipmentInventorySlot.HEAD, SpriteID.EQUIPMENT_SLOT_HEAD);
		sprites.put(EquipmentInventorySlot.CAPE, SpriteID.EQUIPMENT_SLOT_CAPE);
		sprites.put(EquipmentInventorySlot.AMULET, SpriteID.EQUIPMENT_SLOT_NECK);
		sprites.put(EquipmentInventorySlot.WEAPON, SpriteID.EQUIPMENT_SLOT_WEAPON);
		sprites.put(EquipmentInventorySlot.RING, SpriteID.EQUIPMENT_SLOT_RING);
		sprites.put(EquipmentInventorySlot.BODY, SpriteID.EQUIPMENT_SLOT_TORSO);
		sprites.put(EquipmentInventorySlot.SHIELD, SpriteID.EQUIPMENT_SLOT_SHIELD);
		sprites.put(EquipmentInventorySlot.LEGS, SpriteID.EQUIPMENT_SLOT_LEGS);
		sprites.put(EquipmentInventorySlot.GLOVES, SpriteID.EQUIPMENT_SLOT_HANDS);
		sprites.put(EquipmentInventorySlot.BOOTS, SpriteID.EQUIPMENT_SLOT_FEET);
		sprites.put(EquipmentInventorySlot.AMMO, SpriteID.EQUIPMENT_SLOT_AMMUNITION);
		EQUIPMENT_SLOT_SPRITE_MAP = sprites.build();
	}

	private static final BufferedImage PANEL_BACKGROUND = ImageUtil.loadImageResource(PlayerEquipmentPanel.class, "equipment-bars.png");
	private static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);

	private static final Border BORDER_LEFT = new EmptyBorder(0, 15, 0, 0);
	private static final Border BORDER_RIGHT = new EmptyBorder(0, 0, 0, 15);

	@Getter
	private final Map<EquipmentInventorySlot, EquipmentPanelSlot> panelMap = new HashMap<>();
	private EquipmentPanelSlot quiverSlot = null;

	private final ItemManager itemManager;
	private final SpriteManager spriteManager;
	private Quiver quiver;

	public PlayerEquipmentPanel(final GameItem[] items, final Quiver quiver, final SpriteManager spriteManager, final ItemManager itemManager)
	{
		super();

		this.quiver = quiver;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;

		this.setMinimumSize(PANEL_SIZE);
		this.setPreferredSize(PANEL_SIZE);
		this.setLayout(new GridBagLayout());
		this.setBackground(new Color(62, 53, 41));

		spriteManager.getSpriteAsync(SpriteID.EQUIPMENT_SLOT_TILE, 0, img -> SwingUtilities.invokeLater(() -> createPanel(items, img)));
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		final int x = (this.getWidth() - PANEL_BACKGROUND.getWidth()) / 2;
		final int y = (this.getHeight() - PANEL_BACKGROUND.getHeight()) / 2;
		g2d.drawImage(PANEL_BACKGROUND, x, y, null);
	}

	private void createPanel(final GameItem[] items, final BufferedImage background)
	{
		this.removeAll();

		final GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 4;
		c.ipady = 3;
		c.anchor = GridBagConstraints.CENTER;

		// First row
		c.gridx = 1;
		c.gridy = 0;
		createEquipmentPanelSlot(EquipmentInventorySlot.HEAD, items, background, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridx++;
		createEquipmentPanelSlot(null, quiver.getQuiverAmmo(), background, c, null); // Quiver

		// Second row
		c.gridx = 0;
		c.gridy++;
		c.anchor = GridBagConstraints.EAST;
		createEquipmentPanelSlot(EquipmentInventorySlot.CAPE, items, background, c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx++;
		createEquipmentPanelSlot(EquipmentInventorySlot.AMULET, items, background, c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		createEquipmentPanelSlot(EquipmentInventorySlot.AMMO, items, background, c);
		c.anchor = GridBagConstraints.CENTER;

		// Third row
		c.gridx = 0;
		c.gridy++;
		createEquipmentPanelSlot(EquipmentInventorySlot.WEAPON, items, background, c, BORDER_RIGHT);
		c.gridx++;
		createEquipmentPanelSlot(EquipmentInventorySlot.BODY, items, background, c);
		c.gridx++;
		createEquipmentPanelSlot(EquipmentInventorySlot.SHIELD, items, background, c, BORDER_LEFT);

		// Fourth row
		c.gridx = 1;
		c.gridy++;
		createEquipmentPanelSlot(EquipmentInventorySlot.LEGS, items, background, c);

		// Fifth row
		c.gridx = 0;
		c.gridy++;
		createEquipmentPanelSlot(EquipmentInventorySlot.GLOVES, items, background, c, BORDER_RIGHT);
		c.gridx++;
		createEquipmentPanelSlot(EquipmentInventorySlot.BOOTS, items, background, c);
		c.gridx++;
		createEquipmentPanelSlot(EquipmentInventorySlot.RING, items, background, c, BORDER_LEFT);

		this.revalidate();
		this.repaint();
	}

	public void setQuiver(final Quiver quiver)
	{
		this.quiver = quiver;
		if (quiverSlot == null)
		{
			return;
		}

		final GameItem quiverAmmoItem = quiver.getQuiverAmmo();
		quiverSlot.setVisible(quiver.isSlotVisible());

		if (quiverAmmoItem == null)
		{
			quiverSlot.setGameItem(null, null);
			quiverSlot.setToolTipText(null);
			return;
		}

		final AsyncBufferedImage img = itemManager.getImage(quiverAmmoItem.getId(), quiverAmmoItem.getQty(), quiverAmmoItem.isStackable());
		quiverSlot.setGameItem(quiverAmmoItem, img);
		quiverSlot.setToolTipText(quiverAmmoItem.getDisplayName());

		img.onLoaded(() -> {
			quiverSlot.setGameItem(quiverAmmoItem, img);
			quiverSlot.setToolTipText(quiverAmmoItem.getDisplayName());
		});
	}

	private void createEquipmentPanelSlot(final EquipmentInventorySlot slot, final GameItem[] items,
										  final BufferedImage background, final GridBagConstraints c)
	{
		createEquipmentPanelSlot(slot, items, background, c, null);
	}

	private void createEquipmentPanelSlot(final EquipmentInventorySlot slot, final GameItem[] items,
										  final BufferedImage background, final GridBagConstraints constraints, final Border border)
	{
		final GameItem item = (slot != null && items.length > slot.getSlotIdx()) ? items[slot.getSlotIdx()] : null;
		createEquipmentPanelSlot(slot, item, background, constraints, border);
	}

	private void createEquipmentPanelSlot(final EquipmentInventorySlot slot, final GameItem item,
										  final BufferedImage background, final GridBagConstraints constraints, final Border border)
	{
		final int spriteID = slot != null
				? EQUIPMENT_SLOT_SPRITE_MAP.get(slot)
				: EQUIPMENT_SLOT_SPRITE_MAP.get(EquipmentInventorySlot.AMMO); // Quiver fallback

		createEquipmentPanelSlot(slot, item, background, constraints, border, spriteID);
	}

	private void createEquipmentPanelSlot(final EquipmentInventorySlot slot, final GameItem item,
										  final BufferedImage background, final GridBagConstraints constraints, final Border border, final int spriteID)
	{
		final GridBagConstraints c = (GridBagConstraints) constraints.clone();
		final AsyncBufferedImage image = (item != null)
				? itemManager.getImage(item.getId(), item.getQty(), item.isStackable())
				: null;

		spriteManager.getSpriteAsync(spriteID, 0, img -> SwingUtilities.invokeLater(() ->
		{
			final EquipmentPanelSlot panel = new EquipmentPanelSlot(item, image, background, img);
			if (border != null)
			{
				panel.setBorder(border);
			}

			// 🟡 Tooltip Support
			panel.setToolTipText(item != null ? item.getDisplayName() : null);

			if (slot != null)
			{
				panelMap.put(slot, panel);
			}
			else
			{
				quiverSlot = panel;
				panel.setVisible(quiver.isSlotVisible());
			}

			if (image != null)
			{
				image.onLoaded(() ->
				{
					panel.setGameItem(item, image);
					panel.setToolTipText(item.getDisplayName());
				});
			}

			add(panel, c);
		}));
	}
}
