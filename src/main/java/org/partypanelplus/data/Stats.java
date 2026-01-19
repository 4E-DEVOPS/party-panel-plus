package org.partypanelplus.data;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import org.partypanelplus.data.events.PartyPlusStat;

@Getter
@Setter
public class Stats
{
	private final Map<Skill, Integer> baseLevels = new HashMap<>();
	private final Map<Skill, Integer> boostedLevels = new HashMap<>();
	private int specialPercent;
	private int runEnergy;
	private int combatLevel;
	private int totalLevel;

	// Overlays
	private int poison;
	private int venom;
	private int disease;
	private boolean frozen;
	private boolean burning;
	private boolean afk;

	public Stats()
	{
		for (final Skill s : Skill.values())
		{
			baseLevels.put(s, 1);
			boostedLevels.put(s, 1);
		}

		baseLevels.put(Skill.HITPOINTS, 10);
		boostedLevels.put(Skill.HITPOINTS, 10);

		combatLevel = 3;
		specialPercent = 0;
		runEnergy = 0;
		totalLevel = 0;

		poison = 0;
		venom = 0;
		disease = 0;
		frozen = false;
		burning = false;
		afk = false;
	}

	public Stats(@NonNull final Client client)
	{
		final int[] bases = client.getSkillExperiences();
		final int[] boosts = client.getBoostedSkillLevels();
		for (final Skill s : Skill.values())
		{
			baseLevels.put(s, Experience.getLevelForXp(bases[s.ordinal()]));
			boostedLevels.put(s, boosts[s.ordinal()]);
		}

		recalculateCombatLevel();

		specialPercent = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
		totalLevel = client.getTotalLevel();
		runEnergy = client.getEnergy() / 100;

		// NEW additions
		poison = client.getVarpValue(VarPlayer.POISON);
		disease = client.getVarpValue(VarPlayer.DISEASE_VALUE);
		frozen = false;
		burning = false;
		afk = false;
	}

	public int recalculateCombatLevel()
	{
		combatLevel = Experience.getCombatLevel(
				Math.min(baseLevels.get(Skill.ATTACK), 99),
				Math.min(baseLevels.get(Skill.STRENGTH), 99),
				Math.min(baseLevels.get(Skill.DEFENCE), 99),
				Math.min(baseLevels.get(Skill.HITPOINTS), 99),
				Math.min(baseLevels.get(Skill.MAGIC), 99),
				Math.min(baseLevels.get(Skill.RANGED), 99),
				Math.min(baseLevels.get(Skill.PRAYER), 99)
		);

		return combatLevel;
	}

	public PartyPlusStat createPartyStatChangeForSkill(Skill s)
	{
		return new PartyPlusStat(s.ordinal(), baseLevels.get(s), boostedLevels.get(s));
	}
}
