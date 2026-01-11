package org.partypanelplus;

import java.awt.*;
import java.awt.event.KeyEvent;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("partypanelplus")
public interface PartyPlusConfig extends Config {
    // ========== BASIC CONFIGS ==========

    @ConfigItem(
            keyName = "alwaysShowIcon",
            name = "Always show sidebar",
            description = "<html>Controls whether the sidebar icon is always shown (checked) or only shown while inside a party (unchecked)</html>"
    )
    default boolean alwaysShowIcon() {
        return true;
    }

    @ConfigItem(
            keyName = "showPartyControls",
            name = "Show Party Controls",
            description = "<html>Controls whether we display the party control buttons like create and leave party</html>",
            position = 0
    )
    default boolean showPartyControls() {
        return true;
    }

    @ConfigItem(
            keyName = "showPartyPassphrase",
            name = "Show Party Passphrase",
            description = "<html>Controls whether the party passphrase is displayed within the UI<br/>If disabled, and party controls are shown, you can still copy</html>",
            position = 1
    )
    default boolean showPartyPassphrase() {
        return true;
    }

    @ConfigItem(
            keyName = "autoExpandMembers",
            name = "Expand members by default",
            description = "<html>Controls whether party member details are automatically expanded (checked) or collapsed into banners (unchecked)</html>",
            position = 2
    )
    default boolean autoExpandMembers() {
        return false;
    }

    @ConfigItem(
            keyName = "displayVirtualLevels",
            name = "Display Virtual Levels",
            description = "<html>Controls whether we display a player's virtual level as their base level</html>",
            position = 3
    )
    default boolean displayVirtualLevels() {
        return false;
    }

    @ConfigItem(
            keyName = "displayPlayerWorlds",
            name = "Display Player Worlds",
            description = "<html>Controls whether we display the world a player is currently on</html>",
            position = 4
    )
    default boolean displayPlayerWorlds() {
        return true;
    }

    enum OverlayMode {
        NAMEPLATES,
        HIGHLIGHT,
        STATUS,
        NONE
    }
    @ConfigItem(
            keyName = "overlayMode",
            name = "Player Overlay",
            description = "Choose how to display overlays for party members"
    )
    default OverlayMode overlayMode() {
        return OverlayMode.STATUS;
    }

    @ConfigItem(
            keyName = "showOthersOverheads",
            name = "Others Overheads",
            description = "Show overhead prayers for party members"
    )
    default boolean showOthersOverheads()  {
        return true;
    }

    @ConfigItem(
            keyName = "nameplateSize",
            name = "Nameplate Font Size",
            description = "Adjust the nameplate font size"
    )
    default int nameplateSize() { return 14; }

    @ConfigItem(
            keyName = "nameplateBold",
            name = "Bold Font",
            description = "Render nameplates in bold font"
    )
    default boolean nameplateBold() { return true; }

    @ConfigItem(
            keyName = "nameplateColor",
            name = "Nameplate Color",
            description = "Color of player nameplates"
    )
    default Color nameplateColor() { return Color.ORANGE; }

    @ConfigItem(
            keyName = "pingHotkey",
            name = "Ping Hotkey",
            description = "Hold the hotkey to ping a tile",
            position = 20
    )
    default Keybind pingHotkey()
    {
        return new Keybind(KeyEvent.VK_CAPS_LOCK, 0);
    }

    @ConfigItem(
            keyName = "pingSoundDistance",
            name = "Ping Sound Distance",
            description = "Maximum distance other party members hear ping sounds",
            position = 21
    )
    default int pingSoundDistance()
    {
        return 30;
    }
    // ========== HIDDEN FIELDS ==========

    @ConfigItem(
            keyName = "previousPartyId",
            name = "",
            description = "",
            hidden = true
    )
    default String previousPartyId() {
        return "";
    }

    @ConfigItem(
            keyName = "previousPartyId",
            name = "",
            description = "",
            hidden = true
    )
    void setPreviousPartyId(String id);

    @ConfigItem(
            keyName = "syncRate",
            name = "",
            description = "",
            hidden = true
    )
    default int syncRate() {
        return 2000;
    }

    @ConfigItem(
            keyName = "autoDisableRLParty",
            name = "",
            description = "",
            hidden = true
    )
    default boolean autoDisableRLParty() {
        return true;
    }

    // ========== AUTO-JOIN ==========

    @ConfigItem(
            keyName = "joinMode",
            name = "Auto-Join Mode",
            description = "Controls whether to auto-join a party on login"
    )
    default JoinMode joinMode() {
        return JoinMode.WORLD;
    }

    @ConfigItem(
            keyName = "joinDelay",
            name = "Auto-Join Delay",
            description = "Delay in seconds before auto-joining a party after login",
            hidden = true
    )
    default int joinDelay() {
        return 5;
    }

    @ConfigItem(
            keyName = "showJoinedPartyName",
            name = "Log Joined Party Name",
            description = "Show the joined party name in the log for debugging",
            hidden = true
    )
    default boolean showJoinedPartyName() {
        return true;
    }

    @ConfigItem(
            keyName = "customPartyName",
            name = "Custom Party Name",
            description = "Manual party name to join when using Custom mode"
    )
    default String customPartyName() {
        return "";
    }

    // ========== NEW: INCOGNITO & MAP VISIBILITY ==========

    @ConfigItem(
            keyName = "incognitoMode",
            name = "Incognito Mode",
            description = "Prevents your icon from being shown on the world map to other party members"
    )
    default boolean incognitoMode() {
        return false;
    }

    @ConfigItem(
            keyName = "showPlayersAcrossWorlds",
            name = "Cross-World Mapping",
            description = "Map a party member’s icon at their coordinates, regardless of world",
            hidden = true
    )
    default boolean showPlayersAcrossWorlds() {
        return false;
    }

    @ConfigItem(
            keyName = "debugPartySync",
            name = "Debug Party Sync",
            description = "Show detailed logs of party syncing and world map updates"
    )
    default boolean debugPartySync() {
        return false;
    }
}
