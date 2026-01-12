package org.partypanelplus;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.events.FriendsChatChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.FriendsChatManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.events.PartyMemberAvatar;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.party.messages.TilePing;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.partypanelplus.data.GameItem;
import org.partypanelplus.data.PartyPlayer;
import org.partypanelplus.data.PrayerData;
import org.partypanelplus.data.Prayers;
import org.partypanelplus.data.Stats;
import org.partypanelplus.data.events.PartyBatchedChange;
import org.partypanelplus.data.events.PartyMiscChange;
import org.partypanelplus.data.events.PartyStatChange;
import org.partypanelplus.ui.MapOverlay;
import org.partypanelplus.ui.MinimapOverlay;
import org.partypanelplus.ui.PingOverlay;
import org.partypanelplus.ui.PlayerOverlays;
import org.partypanelplus.ui.PlayerPanel;
import org.partypanelplus.ui.prayer.PrayerOverheads;
import org.partypanelplus.ui.prayer.PrayerSprites;

@Slf4j
@PluginDescriptor(name = "Party Panel Plus")

public class PartyPlusPlugin extends Plugin {
    private static final BufferedImage ICON = ImageUtil.loadImageResource(PartyPlusPlugin.class, "icon.png");
    private static final int[] RUNEPOUCH_AMOUNT_VARBITS = {
            Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4,
            Varbits.RUNE_POUCH_AMOUNT5, Varbits.RUNE_POUCH_AMOUNT6
    };
    private static final int[] RUNEPOUCH_RUNE_VARBITS = {
            Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3, Varbits.RUNE_POUCH_RUNE4,
            Varbits.RUNE_POUCH_RUNE5, Varbits.RUNE_POUCH_RUNE6
    };
    public static final int[] RUNEPOUCH_ITEM_IDS = {
            ItemID.RUNE_POUCH, ItemID.RUNE_POUCH_L, ItemID.DIVINE_RUNE_POUCH, ItemID.DIVINE_RUNE_POUCH_L
    };
    public static final Set<Integer> DIZANAS_QUIVER_IDS = ImmutableSet.<Integer>builder()
            .addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(ItemID.DIZANAS_QUIVER)))
            .addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(ItemID.BLESSED_DIZANAS_QUIVER)))
            .addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(ItemID.DIZANAS_MAX_CAPE)))
            .build();

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private KeyManager keyManager;

    @Inject
    private MinimapOverlay minimapOverlay;

    @Inject
    private MapOverlay mapOverlay;

    @Getter
    @Inject
    private PartyPlusConfig config;

    @Inject
    private PingOverlay pingOverlay;

    @Inject
    private PartyService partyService;

    @Inject
    private PlayerOverlays playerOverlays;

    @Inject
    private PluginManager pluginManager;

    @Inject
    private PrayerOverheads prayerOverheads;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    SpriteManager spriteManager;

    @Inject
    ItemManager itemManager;

    @Inject
    private WSClient wsClient;

    @Provides
    PartyPlusConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PartyPlusConfig.class);
    }

    @Getter
    private final Map<Long, PartyPlayer> partyMembers = new HashMap<>();

    @Getter
    private PartyPlayer myPlayer = null;
    private ConfigManager configManager;

    private NavigationButton navButton;
    private boolean addedButton = false;

    private PartyPlus panel;
    private boolean autoJoined = false;
    private Instant loginTime;
    private Instant lastLogout;
    private String lastJoinedParty = null;

    // All events should be deferred to the next game tick
    private PartyBatchedChange currentChange = new PartyBatchedChange();

    @Override
    protected void startUp() throws Exception {
        // ===SHUTDOWN Party PLUGIN===
        for (Plugin plugin : pluginManager.getPlugins())
        {
            if (plugin.getClass().getSimpleName().equals("Party"))
            {
                log.info("Disabling RuneLite Party plugin (replaced by Party Panel Plus)");
                pluginManager.setPluginEnabled(plugin, false);
                break;
            }
        }

        panel = new PartyPlus(this);
        navButton = NavigationButton.builder()
                .tooltip("Party Panel Plus")
                .icon(ICON)
                .priority(7)
                .panel(panel)
                .build();

        wsClient.registerMessage(PartyBatchedChange.class);

        if (isInParty() || config.alwaysShowIcon()) {
            clientToolbar.addNavigation(navButton);
            addedButton = true;
        }

        if (isInParty()) {
            clientThread.invokeLater(() ->
            {
                myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager, clientThread);
                partyService.send(new UserSync());
                partyService.send(partyPlayerAsBatchedChange());
            });
        }

        lastLogout = Instant.now();
    }

    @Override
    protected void shutDown() throws Exception {
        // ===SHUTDOWN Party PLUGIN===
        for (Plugin plugin : pluginManager.getPlugins())
        {
            if (plugin.getClass().getSimpleName().equals("Party"))
            {
                log.info("Re-enabling RuneLite Party plugin after shutdown");
                pluginManager.setPluginEnabled(plugin, true);
                break;
            }
        }

        if (isInParty()) {
            final PartyBatchedChange cleanUserInfo = partyPlayerAsBatchedChange();
            cleanUserInfo.setI(new int[0]);
            cleanUserInfo.setE(new int[0]);
            cleanUserInfo.setM(Collections.emptySet());
            cleanUserInfo.setS(Collections.emptySet());
            cleanUserInfo.setRp(null);
            cleanUserInfo.setQ(new int[0]);
            partyService.send(cleanUserInfo);
        }
        clientToolbar.removeNavigation(navButton);
        addedButton = false;
        partyMembers.clear();
        wsClient.unregisterMessage(PartyBatchedChange.class);
        currentChange = new PartyBatchedChange();
        panel.getPlayerPanelMap().clear();
        lastLogout = null;
    }

    public void autoJoinParty()
    {
        PartySource source = resolvePartyType();
        if (source == null)
        {
            log.warn("Auto-join failed: no valid join source found.");
            return;
        }

        if (source != null && source.name != null && !source.name.trim().isEmpty())
        {
            partyService.changeParty(source.name);
            lastJoinedParty = source.name;
            config.setPreviousPartyId(source.name);
            PartyPlusHistory.appendToHistory(configManager, source.name);

            if (config.showJoinedPartyName())
            {
                log.info("Auto-joined party: " + source.name + " (Source: " + source.source + ")");
            }
        }
    }

    private PartySource resolvePartyType()
    {
        JoinMode mode = config.joinMode();

        // NONE disables auto-join
        if (mode == JoinMode.NONE)
        {
            return null;
        }

        // AUTO mode: try sources in fallback order
        if (mode == JoinMode.AUTO)
        {
            PartySource source;

            source = ClanSource();
            if (source != null) return source;

            source = FriendsSource();
            if (source != null) return source;

            source = WorldSource();
            if (source != null) return source;

            source = CustomSource();
            if (source != null) return source;

            source = PreviousSource();
            if (source != null) return source;

            return null;
        }

        // Otherwise: use selected specific mode
        switch (mode)
        {
            case CLAN:
                return ClanSource();

            case FRIENDS:
                return FriendsSource();

            case CUSTOM:
                return CustomSource();

            case PREVIOUS:
                return PreviousSource();

            default:
                return null;
        }
    }

    public String getFriendsChatOwner() {
        FriendsChatManager fc = client.getFriendsChatManager();
        return fc != null ? fc.getOwner() : null;
    }

    public String getClanChatOwner() {
        ClanChannel cc = client.getClanChannel();
        return cc != null ? cc.getName() : null;
    }

    public String getPartyPassphrase()
    {
        if (partyService.isInParty() == true)
        {
            return partyService.getPartyPassphrase();
        }
        return null;
    }


    private static class PartySource
    {
        private final String name;
        private final String source;

        public PartySource(String name, String source)
        {
            this.name = name;
            this.source = source;
        }
    }
    private PartySource ClanSource()
    {
        ClanChannel clan = client.getClanChannel();
        if (clan != null && clan.getName() != null)
        {
            return new PartySource(clan.getName(), "Clan");
        }
        return null;
    }

    private PartySource FriendsSource()
    {
        FriendsChatManager fc = client.getFriendsChatManager();
        if (fc != null && fc.getOwner() != null)
        {
            return new PartySource(fc.getOwner(), "Friends Chat");
        }
        return null;
    }

    private PartySource WorldSource()
    {
        ClanChannel clan = client.getClanChannel();
        FriendsChatManager fc = client.getFriendsChatManager();
        if (fc != null && clan != null)
        {
            return new PartySource("world-" + client.getWorld(), "World");
        }
        return null;
    }

    private PartySource CustomSource()
    {
        String custom = config.customPartyName();
        if (custom != null && !custom.trim().isEmpty())
        {
            return new PartySource(custom.trim(), "Custom");
        }
        return null;
    }

    private PartySource PreviousSource()
    {
        String prev = config.previousPartyId();
        if (prev != null && !prev.trim().isEmpty())
        {
            return new PartySource(prev.trim(), "Previous");
        }
        return null;
    }

    @Subscribe
    public void onClanChannelChanged(ClanChannelChanged event)
    {
        if (event.getClanChannel() == null || event.getClanChannel().getName() == null)
        {
            return;
        }

        // Only handle if auto-join mode is CLAN or AUTO
        JoinMode mode = config.joinMode();
        if (mode != JoinMode.CLAN && mode != JoinMode.AUTO)
        {
            return;
        }

        final String clanName = event.getClanChannel().getName();
        if (!clanName.equals(lastJoinedParty))
        {
            partyService.changeParty(clanName);
            log.info("Auto-joined party from Clan: " + clanName);
            lastJoinedParty = clanName;
        }
    }

    @Subscribe
    public void onFriendsChatChanged(FriendsChatChanged event)
    {
        FriendsChatManager fc = client.getFriendsChatManager();
        if (fc == null || fc.getOwner() == null)
        {
            return;
        }

        // Only handle if auto-join mode is FRIENDS or AUTO
        JoinMode mode = config.joinMode();
        if (mode != JoinMode.FRIENDS && mode != JoinMode.AUTO)
        {
            return;
        }

        // Don't override Clan if already joined that
        if (mode == JoinMode.AUTO && lastJoinedParty != null && client.getClanChannel() != null
                && lastJoinedParty.equals(client.getClanChannel().getName()))
        {
            return;
        }

        final String fcOwner = fc.getOwner();
        if (!fcOwner.equals(lastJoinedParty))
        {
            partyService.changeParty(fcOwner);
            log.info("Auto-joined party from Friends Chat: " + fcOwner);
            lastJoinedParty = fcOwner;
        }
    }

    @Subscribe
    protected void onConfigChanged(final ConfigChanged c) {
        if (!c.getGroup().equals("partypanelplus")) {
            return;
        }

        if (config.alwaysShowIcon()) {
            if (!addedButton) {
                clientToolbar.addNavigation(navButton);
                addedButton = true;
            }
        } else if (addedButton && !isInParty()) {
            clientToolbar.removeNavigation(navButton);
            addedButton = false;
        }
        addedButton = config.alwaysShowIcon();

        switch (c.getKey()) {
            case "autoExpandMembers":
                panel.updatePartyMembersExpand(config.autoExpandMembers());
                break;
            case "showPartyControls":
                panel.updatePartyControls();
                break;
            case "showPartyPassphrase":
                panel.syncPartyPassphraseVisibility();
                break;
            case "displayVirtualLevels":
                panel.updateDisplayVirtualLevels();
                break;
            case "displayPlayerWorlds":
                panel.updateDisplayPlayerWorlds();
                break;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        final GameState state = event.getGameState();

        // Handle LOGIN_SCREEN
        if (state == GameState.LOGIN_SCREEN) {
            lastLogout = Instant.now();

            // RESET auto-join state
            lastJoinedParty = null;

            // Reset world if needed
            if (myPlayer != null && myPlayer.getWorld() != 0) {
                myPlayer.setWorld(0);
                currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.W, 0));
                partyService.send(currentChange);
                currentChange = new PartyBatchedChange();
            }

            return;
        }

        // Handle LOGGED_IN
        if (state == GameState.LOGGED_IN) {
            // ✅ Schedule auto-join
            loginTime = Instant.now();
            autoJoined = false;

            // Create player if missing
            if (myPlayer == null && partyService.getLocalMember() != null) {
                myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager, clientThread);
                partyService.send(partyPlayerAsBatchedChange());
            }

            // Sync world if different
            if (myPlayer != null) {
                int world = client.getWorld();
                if (myPlayer.getWorld() != world) {
                    myPlayer.setWorld(world);
                    currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.W, world));
                }
            }
        }
    }

    public boolean isInParty() {
        return partyService.isInParty();
    }

    public boolean isLocalPlayer(long id) {
        return partyService.getLocalMember() != null && partyService.getLocalMember().getMemberId() == id;
    }

    @Subscribe
    public void onUserPart(final UserPart event) {
        final PartyPlayer removed = partyMembers.remove(event.getMemberId());
        if (removed != null) {
            SwingUtilities.invokeLater(() -> panel.removePartyPlayer(removed));
        }

        if (addedButton && (!isInParty() || partyService.getMembers().size() == 0) && !config.alwaysShowIcon()) {
            clientToolbar.removeNavigation(navButton);
            addedButton = false;
        }
    }

    @Subscribe
    public void onUserSync(final UserSync event) {
        if (!addedButton) {
            clientToolbar.addNavigation(navButton);
            addedButton = true;
        }

        if (myPlayer != null) {
            final PartyBatchedChange c = partyPlayerAsBatchedChange();
            if (c.isValid()) {
                partyService.send(c);
            }
            return;
        }

        clientThread.invoke(() ->
        {
            myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager, clientThread);
            final PartyBatchedChange c = partyPlayerAsBatchedChange();
            if (c.isValid()) {
                partyService.send(c);
            }
        });
    }

    @Subscribe
    public void onPartyChanged(final PartyChanged event) {
        partyMembers.clear();
        SwingUtilities.invokeLater(() ->
        {
            panel.clearSidebar();
            panel.renderSidebar();
        });
        myPlayer = null;

        panel.updateParty();

        if (!isInParty()) {
            if (!config.alwaysShowIcon()) {
                clientToolbar.removeNavigation(navButton);
                addedButton = false;
            }

            panel.getPlayerPanelMap().clear();
            return;
        } else if (!addedButton) {
            clientToolbar.addNavigation(navButton);
            addedButton = true;
        }

        config.setPreviousPartyId(event.getPassphrase());
    }

    @Subscribe
    public void onTilePing(TilePing ping)
    {
        pingOverlay.addPartyPing(ping);
    }

    @Subscribe
    public void onGameTick(final GameTick tick)
    {
        pingOverlay.tickDown();

        // ✅ NORMAL PARTY UPDATE LOGIC
        if (!isInParty() || client.getLocalPlayer() == null || partyService.getLocalMember() == null)
        {
            return;
        }

        // To reduce server load we should only process changes every X ticks
        if (client.getTickCount() % messageFreq(partyService.getMembers().size()) != 0) {
            return;
        }

        // First time logging in or they changed accounts so resend the entire player object
        if (myPlayer == null || !Objects.equals(client.getLocalPlayer().getName(), myPlayer.getUsername())) {
            myPlayer = new PartyPlayer(partyService.getLocalMember(), client, itemManager, clientThread);
            final PartyBatchedChange c = partyPlayerAsBatchedChange();
            partyService.send(c);
            return;
        }

        if (myPlayer.getStats() == null) {
            myPlayer.updatePlayerInfo(client, itemManager);

            for (final Skill s : Skill.values()) {
                currentChange.getS().add(myPlayer.getStats().createPartyStatChangeForSkill(s));
            }
        } else {
            // Check run energy
            final int energy = (client.getEnergy() / 100);
            if (myPlayer.getStats().getRunEnergy() != energy) {
                myPlayer.getStats().setRunEnergy(energy);
                currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.R, energy));
            }
        }

        if (myPlayer.getPrayers() == null) {
            myPlayer.setPrayers(new Prayers(client));
            final Collection<Prayer> available = new ArrayList<>();
            final Collection<Prayer> enabled = new ArrayList<>();
            final Collection<Prayer> unlocked = new ArrayList<>();
            for (final PrayerSprites p : PrayerSprites.values()) {
                final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
                if (data.isAvailable()) {
                    available.add(p.getPrayer());
                }

                if (data.isEnabled()) {
                    enabled.add(p.getPrayer());
                }

                if (data.isUnlocked()) {
                    unlocked.add(p.getPrayer());
                }
            }

            currentChange.setAp(PartyBatchedChange.pack(available));
            currentChange.setEp(PartyBatchedChange.pack(enabled));
            currentChange.setUp(PartyBatchedChange.pack(unlocked));
        } else {
            final Collection<Prayer> available = new ArrayList<>();
            final Collection<Prayer> enabled = new ArrayList<>();
            final Collection<Prayer> unlocked = new ArrayList<>();
            boolean change = false;
            for (final PrayerSprites p : PrayerSprites.values()) {
                change = myPlayer.getPrayers().updatePrayerState(p, client) || change;

                final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
                if (data.isAvailable()) {
                    available.add(p.getPrayer());
                }

                if (data.isEnabled()) {
                    enabled.add(p.getPrayer());
                }
            }

            if (change) {
                currentChange.setAp(PartyBatchedChange.pack(available));
                currentChange.setEp(PartyBatchedChange.pack(enabled));
                currentChange.setUp(PartyBatchedChange.pack(unlocked));
            }
        }

        if (currentChange.isValid()) {
            currentChange.setMemberId(partyService.getLocalMember().getMemberId());
            currentChange.removeDefaults();
            partyService.send(currentChange);
            currentChange = new PartyBatchedChange();
        }
    }

    @Subscribe
    public void onStatChanged(final StatChanged event) {
        if (myPlayer == null || myPlayer.getStats() == null || !isInParty()) {
            return;
        }

        // Always store the players "real" level using their virtual level so when they change the config the data still exists
        final Skill s = event.getSkill();
        if (myPlayer.getSkillBoostedLevel(s) == event.getBoostedLevel() &&
                Experience.getLevelForXp(event.getXp()) == myPlayer.getSkillRealLevel(s)) {
            return;
        }

        final int virtualLvl = Experience.getLevelForXp(event.getXp());

        myPlayer.setSkillsBoostedLevel(event.getSkill(), event.getBoostedLevel());
        myPlayer.setSkillsRealLevel(event.getSkill(), virtualLvl);

        currentChange.getS().add(new PartyStatChange(event.getSkill().ordinal(), virtualLvl, event.getBoostedLevel()));

        // Total level change
        if (myPlayer.getStats().getTotalLevel() != client.getTotalLevel()) {
            myPlayer.getStats().setTotalLevel(client.getTotalLevel());
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.T, myPlayer.getStats().getTotalLevel()));
        }

        // Combat level change
        final int oldCombatLevel = myPlayer.getStats().getCombatLevel();
        myPlayer.getStats().recalculateCombatLevel();
        if (myPlayer.getStats().getCombatLevel() != oldCombatLevel) {
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.C, myPlayer.getStats().getCombatLevel()));
        }
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged c) {
        if (myPlayer == null || !isInParty()) {
            return;
        }

        if (c.getContainerId() == InventoryID.INVENTORY.getId()) {
            final ItemContainer inventory = c.getItemContainer();
            myPlayer.setInventory(GameItem.convertItemsToGameItems(inventory.getItems(), itemManager));
            int[] items = convertItemsToArray(c.getItemContainer().getItems());
            currentChange.setI(items);

            if (itemContainerHasRunePouch(inventory)) {
                final List<Item> runesInPouch = getRunePouchContents(client);
                myPlayer.setRunesInPouch(GameItem.convertItemsToGameItems(runesInPouch.toArray(new Item[runesInPouch.size()]), itemManager));
                currentChange.setRp(convertRunePouchContentsToPackedInts(runesInPouch));
            }

            // As long as they have the quiver in their inventory and have already worn it we should keep showing it in the UI
            boolean hasQuiverInInventory = false;
            for (final Item item : inventory.getItems()) {
                if (DIZANAS_QUIVER_IDS.contains(item.getId())) {
                    hasQuiverInInventory = true;
                    break;
                }
            }
            myPlayer.getQuiver().setInInventory(hasQuiverInInventory);

            // We may be able to find the quiver ammo when it enters the inventory from a storage container if they've already worn it this game session
            if (hasQuiverInInventory && myPlayer.getQuiver().getQuiverAmmo() == null) {
                updateQuiverAmmo();
            }
        } else if (c.getContainerId() == InventoryID.EQUIPMENT.getId()) {
            myPlayer.setEquipment(GameItem.convertItemsToGameItems(c.getItemContainer().getItems(), itemManager));
            int[] items = convertItemsToArray(c.getItemContainer().getItems());
            currentChange.setE(items);

            final Item cape = c.getItemContainer().getItem(EquipmentInventorySlot.CAPE.getSlotIdx());
            boolean isWearingQuiver = cape != null && DIZANAS_QUIVER_IDS.contains(cape.getId());
            myPlayer.getQuiver().setBeingWorn(isWearingQuiver);

            // When they wear the cape we need to update quiver ammo only if we don't yet know it, the var changes handle tracking
            if (isWearingQuiver && myPlayer.getQuiver().getQuiverAmmo() == null) {
                updateQuiverAmmo();
            }
        }
    }

    private GameItem getQuiverAmmo() {
        final int quiverAmmoId = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_ID);
        final int quiverAmmoCount = client.getVarpValue(VarPlayer.DIZANAS_QUIVER_ITEM_COUNT);
        if (quiverAmmoId == -1 || quiverAmmoCount == 0) {
            return null;
        }

        return new GameItem(quiverAmmoId, quiverAmmoCount, itemManager);
    }

    private void updateQuiverAmmo() {
        final GameItem quiverAmmo = getQuiverAmmo();
        if (quiverAmmo == myPlayer.getQuiver().getQuiverAmmo()) {
            return;
        }

        myPlayer.getQuiver().setQuiverAmmo(quiverAmmo);
        if (quiverAmmo != null) {
            currentChange.setQ(new int[]{quiverAmmo.getId(), quiverAmmo.getQty()});
        } else {
            currentChange.setQ(new int[0]);
        }
    }

    private static boolean itemContainerHasRunePouch(ItemContainer inventory) {
        for (final int id : RUNEPOUCH_ITEM_IDS) {
            if (inventory.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public int[] convertRunePouchContentsToPackedInts(final List<Item> runesInPouch) {
        return runesInPouch.stream()
                .mapToInt(PartyBatchedChange::packRune)
                .toArray();
    }

    public static List<Item> getRunePouchContents(Client client) {
        final EnumComposition runepouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
        final List<Item> items = new ArrayList<>();
        for (int i = 0; i < RUNEPOUCH_AMOUNT_VARBITS.length; i++) {
            @Varbit int amount = client.getVarbitValue(RUNEPOUCH_AMOUNT_VARBITS[i]);
            if (amount <= 0) {
                continue;
            }

            @Varbit int runeId = client.getVarbitValue(RUNEPOUCH_RUNE_VARBITS[i]);
            if (runeId == 0) {
                continue;
            }

            final int itemId = runepouchEnum.getIntValue(runeId);
            items.add(new Item(itemId, amount));
        }

        return items;
    }

    @Subscribe
    public void onVarbitChanged(final VarbitChanged event) {
        if (myPlayer == null || myPlayer.getStats() == null || !isInParty()) {
            return;
        }

        final int specialPercent = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
        if (specialPercent != myPlayer.getStats().getSpecialPercent()) {
            myPlayer.getStats().setSpecialPercent(specialPercent);
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.S, specialPercent));
        }

        final int stamina = client.getVarbitValue(Varbits.STAMINA_EFFECT);
        if (stamina != myPlayer.getStamina()) {
            myPlayer.setStamina(stamina);
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.ST, stamina));
        }

        final int poison = client.getVarpValue(VarPlayer.POISON);
        if (poison != myPlayer.getPoison()) {
            myPlayer.setPoison(poison);
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.P, poison));
        }

        final int disease = client.getVarpValue(VarPlayer.DISEASE_VALUE);
        if (disease != myPlayer.getDisease()) {
            myPlayer.setDisease(disease);
            currentChange.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.D, disease));
        }

        if (ArrayUtils.contains(RUNEPOUCH_RUNE_VARBITS, event.getVarbitId()) || ArrayUtils.contains(RUNEPOUCH_AMOUNT_VARBITS, event.getVarbitId())) {
            List<Item> runePouchContents = getRunePouchContents(client);
            myPlayer.setRunesInPouch(GameItem.convertItemsToGameItems(runePouchContents.toArray(new Item[runePouchContents.size()]), itemManager));
            currentChange.setRp(convertRunePouchContentsToPackedInts(runePouchContents));
        }

        if (event.getVarpId() == VarPlayer.DIZANAS_QUIVER_ITEM_COUNT || event.getVarpId() == VarPlayer.DIZANAS_QUIVER_ITEM_ID) {
            updateQuiverAmmo();
        }
    }

    @Subscribe
    public void onPartyBatchedChange(PartyBatchedChange e) {
        if (isLocalPlayer(e.getMemberId())) {
            return;
        }

        // create new PartyPlayer for this member if they don't already exist
        final PartyPlayer player = partyMembers.computeIfAbsent(e.getMemberId(), k -> {
            PartyPlayer p = new PartyPlayer(partyService.getMemberById(e.getMemberId()));
            p.setPlayerColor(getRandomPartyColor()); // ✅ Assign color
            return p;
        });

        // Create placeholder stats object
        if (player.getStats() == null && e.hasStatChange()) {
            player.setStats(new Stats());
        }

        // Create placeholder prayer object
        if (player.getPrayers() == null && (e.getAp() != null || e.getEp() != null || e.getUp() != null)) {
            player.setPrayers(new Prayers());
        }
        clientThread.invoke(() ->
        {
            e.process(player, itemManager);

            SwingUtilities.invokeLater(() ->
            {
                panel.drawPlayerPanel(player, e.hasBreakingBannerChange());
            });
        });
    }

    @Subscribe
    public void onPartyMemberAvatar(PartyMemberAvatar e) {
        if (isLocalPlayer(e.getMemberId()) || partyMembers.get(e.getMemberId()) == null) {
            return;
        }

        final PartyPlayer player = partyMembers.get(e.getMemberId());
        player.getMember().setAvatar(e.getImage());
        SwingUtilities.invokeLater(() ->
        {
            final PlayerPanel p = panel.getPlayerPanelMap().get(e.getMemberId());
            if (p != null) {
                p.getBanner().refreshStats();
            }
        });
    }

    private static final Color[] PARTY_COLORS = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN,
            Color.CYAN, Color.BLUE, Color.MAGENTA, Color.PINK
    };

    private Color getRandomPartyColor()
    {
        return PARTY_COLORS[(int)(Math.random() * PARTY_COLORS.length)];
    }

    public Color getColorForMember(long memberId)
    {
        if (isLocalPlayer(memberId) && myPlayer != null)
        {
            return myPlayer.getPlayerColor();
        }

        PartyPlayer player = partyMembers.get(memberId);
        if (player != null && player.getPlayerColor() != null)
        {
            return player.getPlayerColor();
        }

        return Color.ORANGE;
    }

    public void leaveParty() {
        partyService.changeParty(null);
        panel.updateParty();
    }

    private int[] convertItemsToArray(Item[] items) {
        int[] eles = new int[items.length * 2];
        for (int i = 0; i < items.length * 2; i += 2) {
            if (items[i / 2] == null) {
                eles[i] = -1;
                eles[i + 1] = 0;
                continue;
            }

            eles[i] = items[i / 2].getId();
            eles[i + 1] = items[i / 2].getQuantity();
        }

        return eles;
    }

    private int[] convertGameItemsToArray(GameItem[] items) {
        int[] eles = new int[items.length * 2];
        for (int i = 0; i < items.length * 2; i += 2) {
            if (items[i / 2] == null) {
                eles[i] = -1;
                eles[i + 1] = 0;
                continue;
            }

            eles[i] = items[i / 2].getId();
            eles[i + 1] = items[i / 2].getQty();
        }

        return eles;
    }

    public PartyBatchedChange partyPlayerAsBatchedChange() {
        final PartyBatchedChange c = new PartyBatchedChange();
        if (myPlayer == null) {
            return c;
        }

        // Inventories
        c.setI(convertGameItemsToArray(myPlayer.getInventory()));
        c.setE(convertGameItemsToArray(myPlayer.getEquipment()));

        // Stats
        if (myPlayer.getStats() != null) {
            for (final Skill s : Skill.values()) {
                c.getS().add(myPlayer.getStats().createPartyStatChangeForSkill(s));
            }

            c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.S, myPlayer.getStats().getSpecialPercent()));
            c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.R, myPlayer.getStats().getRunEnergy()));
            c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.C, myPlayer.getStats().getCombatLevel()));
            c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.T, myPlayer.getStats().getTotalLevel()));
        }

        // Misc
        c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.ST, myPlayer.getStamina()));
        c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.P, myPlayer.getPoison()));
        c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.D, myPlayer.getDisease()));
        c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.W, myPlayer.getWorld()));

        // Prayers
        if (myPlayer.getPrayers() != null) {
            final Collection<Prayer> available = new ArrayList<>();
            final Collection<Prayer> enabled = new ArrayList<>();
            final Collection<Prayer> unlocked = new ArrayList<>();
            for (final PrayerSprites p : PrayerSprites.values()) {
                final PrayerData data = myPlayer.getPrayers().getPrayerData().get(p.getPrayer());
                if (data.isAvailable()) {
                    available.add(p.getPrayer());
                }

                if (data.isEnabled()) {
                    enabled.add(p.getPrayer());
                }

                if (data.isUnlocked()) {
                    unlocked.add(p.getPrayer());
                }
            }

            c.setAp(PartyBatchedChange.pack(available));
            c.setEp(PartyBatchedChange.pack(enabled));
            c.setUp(PartyBatchedChange.pack(unlocked));
        }

        c.getM().add(new PartyMiscChange(PartyMiscChange.PartyMisc.U, myPlayer.getUsername()));

        if (client.isClientThread()) {
            c.setRp(convertRunePouchContentsToPackedInts(getRunePouchContents(client)));
        }

        final GameItem quiverAmmo = myPlayer.getQuiver().getQuiverAmmo();
        if (quiverAmmo != null) {
            c.setQ(new int[]{quiverAmmo.getId(), quiverAmmo.getQty()});
        } else {
            c.setQ(new int[0]);
        }

        c.setMemberId(partyService.getLocalMember().getMemberId()); // Add member ID before sending
        c.removeDefaults();

        return c;
    }

    @Schedule(
            period = 10,
            unit = ChronoUnit.SECONDS
    )
    public void checkIdle() {
        if (client.getGameState() != GameState.LOGIN_SCREEN) {
            return;
        }

        if (lastLogout != null && lastLogout.isBefore(Instant.now().minus(30, ChronoUnit.MINUTES))
                && partyService.isInParty()) {
            log.info("Leaving party due to inactivity");
            partyService.changeParty(null);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!isInParty())
            return;

        if (!event.getMenuOption().equalsIgnoreCase("Walk here"))
            return;

        // Get the tile the user clicked
        WorldPoint target = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
        if (target == null)
            return;

        // Send RuneLite's TilePing to party (no color)
        partyService.send(new TilePing(target));

        // Get color for this member
        Color color = getColorForMember(partyService.getLocalMember().getMemberId());
        if (color == null)
            color = Color.ORANGE;
        // Send RuneLite's TilePing over the network (no color)
        partyService.send(new TilePing(target));

        // Show local ping immediately (colored + timed)
        pingOverlay.addPartyPing(target, color, 6);
    }

    private static int messageFreq(int partySize)
    {
        // Introduce a tick delay for each member >6
        // Default the message frequency to every 2 ticks since this plugin sends a lot of data
        return Math.max(2, partySize - 6);
    }
}
