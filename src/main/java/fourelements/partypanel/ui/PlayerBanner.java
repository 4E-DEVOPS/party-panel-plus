package fourelements.partypanel.ui;

import com.google.common.base.Strings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import fourelements.partypanel.data.PartyPlayer;

public class PlayerBanner extends JPanel
{
	private static final Dimension STAT_ICON_SIZE = new Dimension(18, 18);
	private static final Dimension ICON_SIZE = new Dimension(Constants.ITEM_SPRITE_WIDTH - 6, Constants.ITEM_SPRITE_HEIGHT - 4);
	private static final BufferedImage EXPAND_ICON = ImageUtil.loadImageResource(PlayerPanel.class, "expand.png");
	private static final String SPECIAL_ATTACK_NAME = "Special Attack";
	private static final String RUN_ENERGY_NAME = "Run Energy";

	@Getter
	private final JPanel statsPanel = new JPanel();
	private final JLabel iconLabel = new JLabel();
	private final Map<String, JLabel> statLabels = new HashMap<>();
	private final Map<String, JLabel> iconLabels = new HashMap<>();
	@Getter
	private final JLabel expandIcon = new JLabel();
	private final JLabel worldLabel = new JLabel();

	private final ImageIcon expandIconUp;
	private final ImageIcon expandIconDown;

	@Setter
	@Getter
	private PartyPlayer player;
	private boolean checkIcon;

	private BufferedImage currentHeart = null;
	private boolean usingStamIcon;

	public PlayerBanner(final PartyPlayer player, boolean expanded, boolean displayWorld, SpriteManager spriteManager)
	{
		super();
		this.player = player;

		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 14, 68));
		this.setBorder(new EmptyBorder(5, 5, 0,  5));

		statsPanel.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 25));
		statsPanel.setLayout(new GridLayout(0, 4));
		statsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		statsPanel.setOpaque(true);

		expandIconDown = new ImageIcon(EXPAND_ICON);
		expandIconUp = new ImageIcon(ImageUtil.rotateImage(EXPAND_ICON, Math.PI));
		expandIcon.setIcon(expanded ? expandIconUp : expandIconDown);

		worldLabel.setHorizontalTextPosition(JLabel.LEFT);
		worldLabel.setVisible(displayWorld);
		worldLabel.setToolTipText("Player's current world");

		usingStamIcon = player.getStamina() > 0;
		statsPanel.add(createIconPanel(spriteManager, SpriteID.SKILL_HITPOINTS, Skill.HITPOINTS.getName(), String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS))));
		statsPanel.add(createIconPanel(spriteManager, SpriteID.SKILL_PRAYER, Skill.PRAYER.getName(), String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER))));
		statsPanel.add(createIconPanel(spriteManager, SpriteID.MULTI_COMBAT_ZONE_CROSSED_SWORDS, SPECIAL_ATTACK_NAME, player.getStats() == null ? "0" : String.valueOf(player.getStats().getSpecialPercent())));
		statsPanel.add(createIconPanel(spriteManager,
				usingStamIcon ? SpriteID.MINIMAP_ORB_RUN_ICON_SLOWED_DEPLETION : SpriteID.MINIMAP_ORB_RUN_ICON,
				RUN_ENERGY_NAME, player.getStats() == null ? "0" : String.valueOf(player.getStats().getRunEnergy()))
		);

		recreatePanel();
	}

	public void setExpandIcon(boolean direction)
	{
		expandIcon.setIcon(direction ? expandIconUp : expandIconDown);
	}

	public void recreatePanel()
	{
		removeAll();

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1.0;
		c.ipady = 4;

		iconLabel.setBorder(new MatteBorder(1, 1, 1, 1, ColorScheme.DARKER_GRAY_HOVER_COLOR));
		iconLabel.setPreferredSize(ICON_SIZE);
		iconLabel.setMinimumSize(ICON_SIZE);
		iconLabel.setOpaque(false);

		checkIcon = player.getMember() == null || player.getMember().getAvatar() == null;
		if (!checkIcon)
		{
			addIcon();
		}

		add(iconLabel, c);
		c.gridx++;

		final JPanel nameContainer = new JPanel(new GridLayout(2, 1));
		nameContainer.setBorder(new EmptyBorder(0, 5, 0, 0));
		nameContainer.setOpaque(false);

		final JLabel usernameLabel = new JLabel();
		usernameLabel.setLayout(new OverlayLayout(usernameLabel));
		usernameLabel.setHorizontalTextPosition(JLabel.LEFT);

		if (Strings.isNullOrEmpty(player.getUsername()))
		{
			usernameLabel.setText("Not logged in");
			worldLabel.setText("");
		}
		else
		{
			final String levelText = player.getStats() == null ? "" : " (level-" + player.getStats().getCombatLevel() + ")";
			usernameLabel.setText(player.getUsername() + levelText);
			worldLabel.setText(player.getWorld() > 0 ? "World " + player.getWorld() : "");
		}

		expandIcon.setAlignmentX(Component.RIGHT_ALIGNMENT);
		usernameLabel.add(expandIcon, BorderLayout.EAST);
		nameContainer.add(usernameLabel);
		nameContainer.add(worldLabel);

		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(nameContainer, c);

		refreshStats();

		c.gridy++;
		c.weightx = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		add(statsPanel, c);

		revalidate();
		repaint();
	}

	private void addIcon()
	{
		if (player.getMember() == null || player.getMember().getAvatar() == null)
		{
			return;
		}

		final BufferedImage resized = ImageUtil.resizeImage(player.getMember().getAvatar(), Constants.ITEM_SPRITE_WIDTH - 8, Constants.ITEM_SPRITE_HEIGHT - 4);
		iconLabel.setIcon(new ImageIcon(resized));
	}

	public void refreshStats()
	{
		if (checkIcon && player.getMember() != null && player.getMember().getAvatar() != null)
		{
			addIcon();
			checkIcon = false;
		}

		statLabels.getOrDefault(Skill.HITPOINTS.getName(), new JLabel()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.HITPOINTS)));
		statLabels.getOrDefault(Skill.PRAYER.getName(), new JLabel()).setText(String.valueOf(player.getSkillBoostedLevel(Skill.PRAYER)));
		statLabels.getOrDefault(SPECIAL_ATTACK_NAME, new JLabel()).setText(player.getStats() == null ? "0" : String.valueOf(player.getStats().getSpecialPercent()));
		statLabels.getOrDefault(RUN_ENERGY_NAME, new JLabel()).setText(player.getStats() == null ? "0" : String.valueOf(player.getStats().getRunEnergy()));

		statsPanel.revalidate();
		statsPanel.repaint();
	}

	private JPanel createIconPanel(final SpriteManager spriteManager, final int spriteID, final String name, final String value)
	{
		final JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(STAT_ICON_SIZE);
		iconLabels.put(name, iconLabel);
		setSpriteIcon(name, spriteID, spriteManager);

		final JLabel textLabel = new JLabel(value);
		textLabel.setHorizontalAlignment(JLabel.CENTER);
		textLabel.setHorizontalTextPosition(JLabel.CENTER);
		statLabels.put(name, textLabel);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(iconLabel, BorderLayout.WEST);
		panel.add(textLabel, BorderLayout.CENTER);
		panel.setOpaque(false);
		panel.setToolTipText(name);

		return panel;
	}

	private void setSpriteIcon(String statLabelKey, final int spriteID, final SpriteManager spriteManager)
	{
		final JLabel label = iconLabels.get(statLabelKey);
		spriteManager.getSpriteAsync(spriteID, 0, img ->
				SwingUtilities.invokeLater(() ->
				{
					if (spriteID == SpriteID.SKILL_PRAYER)
					{
						label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, STAT_ICON_SIZE.width + 2, STAT_ICON_SIZE.height + 2)));
					}
					else
					{
						label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, STAT_ICON_SIZE.width, STAT_ICON_SIZE.height)));
					}
					label.revalidate();
					label.repaint();
				}));
	}

	private void setBufferedIcon(String statLabelKey, final BufferedImage img)
	{
		final JLabel label = iconLabels.get(statLabelKey);
		SwingUtilities.invokeLater(() ->
		{
			label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, STAT_ICON_SIZE.width, STAT_ICON_SIZE.height)));
			label.revalidate();
			label.repaint();
		});
	}

	public void setCurrentHeart(final BufferedImage img, SpriteManager spriteManager)
	{
		if ((img == null && currentHeart == null) || (img != null && img.equals(currentHeart)))
		{
			return;
		}
		currentHeart = img;
		if (currentHeart == null)
		{
			setSpriteIcon(Skill.HITPOINTS.getName(), SpriteID.SKILL_HITPOINTS, spriteManager);
		}
		else
		{
			setBufferedIcon(Skill.HITPOINTS.getName(), currentHeart);
		}
		statsPanel.revalidate();
		statsPanel.repaint();
	}

	public void setUsingStamIcon(final boolean isStaminaPotted, SpriteManager spriteManager)
	{
		if (isStaminaPotted == usingStamIcon)
		{
			return;
		}

		usingStamIcon = isStaminaPotted;
		final int id = usingStamIcon ? SpriteID.MINIMAP_ORB_RUN_ICON_SLOWED_DEPLETION : SpriteID.MINIMAP_ORB_RUN_ICON;
		setSpriteIcon(RUN_ENERGY_NAME, id, spriteManager);
		statsPanel.revalidate();
		statsPanel.repaint();
	}

	public void updateWorld(int world, boolean displayWorlds)
	{
		worldLabel.setVisible(displayWorlds);
		worldLabel.setText("World " + world);
	}
}
