package org.partypanelplus.ui.equipment;

import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import lombok.Getter;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import org.partypanelplus.ImgUtil;
import org.partypanelplus.data.GameItem;

public class EquipmentPanelSlot extends JLabel
{
	private static final int IMAGE_SIZE = 48; // 50% larger than default 32x32 background
	private final BufferedImage background;
	private final BufferedImage placeholder;

	@Getter
	private GameItem item = null;

	EquipmentPanelSlot(final GameItem item, final BufferedImage image, final BufferedImage background, final BufferedImage placeholder)
	{
		super();

		this.background = background;
		this.placeholder = ImageUtil.resizeImage(ImgUtil.overlapImages(placeholder, background, false), IMAGE_SIZE, IMAGE_SIZE);

		setVerticalAlignment(JLabel.CENTER);
		setHorizontalAlignment(JLabel.CENTER);
		setGameItem(item, image); // Initial setup
	}

	public void setGameItem(final GameItem item, final BufferedImage image)
	{
		this.item = item;

		if (item == null || image == null)
		{
			setIcon(new ImageIcon(placeholder));
			setToolTipText(null);
			return;
		}

		// Compose the item icon over background and resize
		BufferedImage combined = ImgUtil.overlapImages(image, background, true);
		setIcon(new ImageIcon(ImageUtil.resizeImage(combined, IMAGE_SIZE, IMAGE_SIZE)));

		// Tooltip with name and optional quantity
		String name = item.getName() != null ? item.getName() : "Unknown item";
		if (item.getQty() > 1)
		{
			name += " x " + QuantityFormatter.formatNumber(item.getQty());
		}
		setToolTipText(name);
	}
}
