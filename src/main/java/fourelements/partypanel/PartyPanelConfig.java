package fourelements.partypanel;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("partypanelplus")
public interface PartyPanelConfig extends Config
{
	// ========== EXISTING CONFIGS ==========

	@ConfigItem(
			keyName = "alwaysShowIcon",
			name = "Always show sidebar",
			description = "<html>Controls whether the sidebar icon is always shown (checked) or only shown while inside a party (unchecked)</html>"
	)
	default boolean alwaysShowIcon()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showPartyControls",
			name = "Show Party Controls",
			description = "<html>Controls whether we display the party control buttons like create and leave party</html>",
			position = 0
	)
	default boolean showPartyControls()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showPartyPassphrase",
			name = "Show Party Passphrase",
			description = "<html>Controls whether the party passphrase is displayed within the UI<br/>If disabled and party controls are shown you can still copy</html>",
			position = 1
	)
	default boolean showPartyPassphrase()
	{
		return true;
	}

	@ConfigItem(
			keyName = "autoExpandMembers",
			name = "Expand members by default",
			description = "<html>Controls whether party member details are automatically expanded (checked) or collapsed into banners (unchecked)</html>",
			position = 2
	)
	default boolean autoExpandMembers()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayVirtualLevels",
			name = "Display Virtual Levels",
			description = "<html>Controls whether we display a player's virtual level as their base level</html>",
			position = 3
	)
	default boolean displayVirtualLevels()
	{
		return false;
	}

	@ConfigItem(
			keyName = "displayPlayerWorlds",
			name = "Display Player Worlds",
			description = "<html>Controls whether we display the world a player is currently on</html>",
			position = 4
	)
	default boolean displayPlayerWorlds()
	{
		return true;
	}

	// ========== HIDDEN FIELDS ==========

	@ConfigItem(
			keyName = "previousPartyId",
			name = "",
			description = "",
			hidden = true
	)
	default String previousPartyId()
	{
		return "";
	}

	@ConfigItem(
			keyName = "previousPartyId",
			name = "",
			description = "",
			hidden = true
	)
	void setPreviousPartyId(String id);


	// ========== NEW: AUTO-JOIN ==========

	@ConfigItem(
			keyName = "joinMode",
			name = "Auto-Join Mode",
			description = "Determines which party to auto-join on login"
	)
	default JoinMode joinMode()
	{
		return JoinMode.CLAN;
	}

	@ConfigItem(
			keyName = "joinDelay",
			name = "Auto-Join Delay (sec)",
			description = "Delay in seconds before auto-joining a party after login"
	)
	default int joinDelay()
	{
		return 3;
	}

	@ConfigItem(
			keyName = "showJoinedPartyName",
			name = "Log Joined Party Name",
			description = "Show the joined party name in the log for debugging"
	)
	default boolean showJoinedPartyName()
	{
		return false;
	}

	@ConfigItem(
			keyName = "customPartyName",
			name = "Custom Party Name",
			description = "Manual party name to join when using Custom mode"
	)
	default String customPartyName()
	{
		return "";
	}
}
