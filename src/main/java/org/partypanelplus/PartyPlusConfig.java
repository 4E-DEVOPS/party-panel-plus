package org.partypanelplus;

import java.awt.*;
import java.awt.event.KeyEvent;
import net.runelite.client.config.*;

@ConfigGroup("partypanelplus")
public interface PartyPlusConfig extends Config
{
    // ========== UI OPTIONS ==========

    @ConfigItem(
            keyName = "alwaysShowIcon",
            name = "Always Show Sidebar",
            description = "Show the Party Panel Plus icon always, or only while in a party"
    )
    default boolean alwaysShowIcon() { return true; }

    @ConfigItem(
            keyName = "showPartyControls",
            name = "Show Party Controls",
            description = "Display create/leave party buttons"
    )
    default boolean showPartyControls() { return true; }

    @ConfigItem(
            keyName = "showPartyPassphrase",
            name = "Show Party Passphrase",
            description = "Show the party passphrase on the panel"
    )
    default boolean showPartyPassphrase() { return true; }

    @ConfigItem(
            keyName = "autoExpandMembers",
            name = "Expand Members by Default",
            description = "Expand each party member's panel by default"
    )
    default boolean autoExpandMembers() { return false; }

    @ConfigItem(
            keyName = "displayVirtualLevels",
            name = "Display Virtual Levels",
            description = "Show virtual levels instead of base"
    )
    default boolean displayVirtualLevels() { return false; }

    @ConfigItem(
            keyName = "displayPlayerWorlds",
            name = "Display Player Worlds",
            description = "Show the current world number next to each member"
    )
    default boolean displayPlayerWorlds() { return true; }

    // ========== PLAYER OVERLAYS ==========

    enum OverlayMode {
        NAMEPLATES, HIGHLIGHT, STATUS, NONE
    }

    @ConfigItem(
            keyName = "overlayMode",
            name = "Player Overlay Mode",
            description = "How party members appear in 3D view"
    )
    default OverlayMode overlayMode() { return OverlayMode.STATUS; }

    @ConfigItem(
            keyName = "showOthersOverheads",
            name = "Show Others' Overhead Prayers",
            description = "Enable to see party member overhead prayers"
    )
    default boolean showOthersOverheads() { return true; }

    @ConfigItem(
            keyName = "nameplateSize",
            name = "Nameplate Font Size",
            description = "Set the nameplate font size"
    )
    default int nameplateSize() { return 14; }

    @ConfigItem(
            keyName = "nameplateBold",
            name = "Bold Nameplate Font",
            description = "Render nameplates in bold"
    )
    default boolean nameplateBold() { return true; }

    @ConfigItem(
            keyName = "nameplateColor",
            name = "Nameplate Color",
            description = "Color of player nameplates"
    )
    default Color nameplateColor() { return Color.ORANGE; }

    // ========== PING SETTINGS ==========

    @ConfigItem(
            keyName = "pingHotkey",
            name = "Ping Hotkey",
            description = "Hotkey to ping a location"
    )
    default Keybind pingHotkey() { return new Keybind(KeyEvent.VK_CAPS_LOCK, 0); }

    @ConfigItem(
            keyName = "pingSoundDistance",
            name = "Ping Sound Distance",
            description = "Distance where ping sounds can be heard"
    )
    default int pingSoundDistance() { return 30; }

    // ========== PARTY MANAGEMENT ==========

    @ConfigItem(
            keyName = "joinMode",
            name = "Auto-Join Mode",
            description = "Set how to join a party automatically on login"
    )
    default JoinMode joinMode() { return JoinMode.AUTO; }

    @ConfigItem(
            keyName = "customPartyName",
            name = "Custom Party Name",
            description = "Party name to join in CUSTOM mode"
    )
    default String customPartyName() { return ""; }

    @ConfigItem(
            keyName = "showJoinedPartyName",
            name = "Log Joined Party Name",
            description = "Log the joined party name to the console (debug)"
    )
    default boolean showJoinedPartyName() { return true; }

    // Removed joinDelay — now hardcoded in plugin logic

    // ========== ADVANCED / DEBUG OPTIONS ==========

    @ConfigItem(
            keyName = "incognitoMode",
            name = "Incognito Mode",
            description = "Prevents your player icon from appearing on world map"
    )
    default boolean incognitoMode() { return false; }

    @ConfigItem(
            keyName = "showPlayersAcrossWorlds",
            name = "Cross-World Mapping",
            description = "Show map icons even if party members are in another world",
            hidden = true
    )
    default boolean showPlayersAcrossWorlds() { return false; }

    @ConfigItem(
            keyName = "debugPartySync",
            name = "Debug Party Sync",
            description = "Enable debug output for sync and map events"
    )
    default boolean debugPartySync() { return false; }

    // ========== VISUAL ENHANCEMENTS ==========

    @ConfigItem(
            keyName = "minimapDotColor",
            name = "Minimap Dot Color",
            description = "Change the minimap dot color for party members"
    )
    default Color minimapDotColor() { return Color.ORANGE; }

    @ConfigItem(
            keyName = "fallbackAvatars",
            name = "Use Fallback Avatars",
            description = "Generate fallback avatars for users without Discord icons"
    )
    default boolean fallbackAvatars() { return true; }

    // ========== HISTORY / STATE ==========

    @ConfigItem(
            keyName = "previousPartyId",
            name = "",
            description = "",
            hidden = true
    )
    default String previousPartyId() { return ""; }

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
    default int syncRate() { return 2000; }

    // ❌ Removed: `autoDisableRLParty()` – deprecated by your new plan to **keep RL Party enabled**
}