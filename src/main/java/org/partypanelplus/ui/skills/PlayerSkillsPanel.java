package org.partypanelplus.ui.skills;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.partypanelplus.data.PartyPlayer;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.runelite.api.Skill.*;

@Getter
public class PlayerSkillsPanel extends JPanel
{
	/**
	 * Skills ordered in the way they should be displayed in the panel.
	 */
	private static final List<Skill> SKILLS = ImmutableList.of(
			ATTACK, HITPOINTS, MINING,
			STRENGTH, AGILITY, SMITHING,
			DEFENCE, HERBLORE, FISHING,
			RANGED, THIEVING, COOKING,
			PRAYER, CRAFTING, FIREMAKING,
			MAGIC, FLETCHING, WOODCUTTING,
			RUNECRAFT, SLAYER, FARMING,
			CONSTRUCTION, HUNTER, SAILING
	);

	private static final ImmutableMap<Skill, Integer> SPRITE_MAP;

	static
	{
		final ImmutableMap.Builder<Skill, Integer> map = ImmutableMap.builder();
		map.put(ATTACK, SpriteID.Staticons.ATTACK);
		map.put(STRENGTH, SpriteID.Staticons.STRENGTH);
		map.put(DEFENCE, SpriteID.Staticons.DEFENCE);
		map.put(RANGED, SpriteID.Staticons.RANGED);
		map.put(PRAYER, SpriteID.Staticons.PRAYER);
		map.put(MAGIC, SpriteID.Staticons.MAGIC);
		map.put(HITPOINTS, SpriteID.Staticons.HITPOINTS);
		map.put(AGILITY, SpriteID.Staticons.AGILITY);
		map.put(HERBLORE, SpriteID.Staticons.HERBLORE);
		map.put(THIEVING, SpriteID.Staticons.THIEVING);
		map.put(CRAFTING, SpriteID.Staticons.CRAFTING);
		map.put(FLETCHING, SpriteID.Staticons.FLETCHING);
		map.put(MINING, SpriteID.Staticons.MINING);
		map.put(SMITHING, SpriteID.Staticons.SMITHING);
		map.put(FISHING, SpriteID.Staticons.FISHING);
		map.put(COOKING, SpriteID.Staticons.COOKING);
		map.put(FIREMAKING, SpriteID.Staticons.FIREMAKING);
		map.put(WOODCUTTING, SpriteID.Staticons.WOODCUTTING);
		map.put(RUNECRAFT, SpriteID.Staticons2.RUNECRAFT);
		map.put(SLAYER, SpriteID.Staticons2.SLAYER);
		map.put(FARMING, SpriteID.Staticons2.FARMING);
		map.put(CONSTRUCTION, SpriteID.Staticons2.CONSTRUCTION);
		map.put(HUNTER, SpriteID.Staticons2.HUNTER);
		map.put(SAILING, SpriteID.Staticons2.SAILING);
		SPRITE_MAP = map.build();
	}

	protected static final Dimension PANEL_SIZE = new Dimension(PluginPanel.PANEL_WIDTH - 14, 296);

	private final Map<Skill, SkillPanelSlot> panelMap = new HashMap<>();
	private final TotalPanelSlot totalLevelPanel;

	private final JPanel skillsPanel = new JPanel();

	public PlayerSkillsPanel(final PartyPlayer player, final boolean displayVirtualLevels, final SpriteManager spriteManager)
	{
		super();

		this.setMinimumSize(PANEL_SIZE);
		this.setPreferredSize(PANEL_SIZE);
		this.setBackground(new Color(62, 53, 41));

		this.setLayout(new DynamicGridLayout(2, 1, 0, 0));

		skillsPanel.setLayout(new DynamicGridLayout(8, 3, 2, 0));
		skillsPanel.setBackground(new Color(62, 53, 41));

		int totalLevel = 0;

		for (final Skill s : SKILLS)
		{
			int realLevel = player.getStats() != null ? player.getSkillRealLevel(s, displayVirtualLevels) : (s == HITPOINTS ? 10 : 1);
			final SkillPanelSlot slot = new SkillPanelSlot(player.getSkillBoostedLevel(s), realLevel);
			panelMap.put(s, slot);
			skillsPanel.add(slot);

			Integer spriteId = SPRITE_MAP.get(s);
			if (spriteId != null)
			{
				spriteManager.getSpriteAsync(spriteId, 0, img ->
				{
					if (img == null)
					{
						System.err.println("Failed to load sprite for: " + s.getName());
						return;
					}
					SwingUtilities.invokeLater(() -> slot.initImages(img, spriteManager));
				});
			}

			updateSkill(player, s, displayVirtualLevels);

			if (player.getStats() != null)
			{
				totalLevel += realLevel;
			}
		}

		this.add(skillsPanel);

		if (player.getStats() == null)
		{
			// HP starts at 10, 22 other skills at lvl 1
			totalLevel = 10 + (Skill.values().length - 1);
		}

		totalLevelPanel = new TotalPanelSlot(totalLevel, spriteManager);
		this.add(totalLevelPanel);
	}

	public void updateSkill(final PartyPlayer player, final Skill s, final boolean displayVirtualLevels)
	{
		int boosted = player.getStats() != null ? player.getSkillBoostedLevel(s) : (s == HITPOINTS ? 10 : 1);
		int baseLevel = player.getStats() != null ? player.getSkillRealLevel(s, displayVirtualLevels) : (s == HITPOINTS ? 10 : 1);

		final SkillPanelSlot panel = panelMap.get(s);
		panel.updateBoostedLevel(boosted);
		panel.updateBaseLevel(baseLevel);

		// No tooltip used (we opted for clean UI)
	}

	public void updateTotalLevel(final int totalLevel)
	{
		totalLevelPanel.updateTotalLevel(totalLevel);
	}
}
