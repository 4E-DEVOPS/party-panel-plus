package org.partypanelplus;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import org.partypanelplus.data.PartyPlayer;
import org.partypanelplus.ui.ControlsPanel;
import org.partypanelplus.ui.PlayerPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class PartyPlus extends PluginPanel {
    private final PartyPlusPlugin plugin;

    @Getter
    private final HashMap<Long, PlayerPanel> playerPanelMap = new HashMap<>();

    private final JPanel basePanel = new JPanel();
    private final JPanel passphrasePanel = new JPanel();
    private final JLabel passphraseLabel = new JLabel();
    private final JLabel noMembersLabel = new JLabel("There are no members in your party");

    @Getter
    private final ControlsPanel controlsPanel;

    @Inject
    PartyPlus(final PartyPlusPlugin plugin) {
        super(false);
        this.plugin = plugin;
        this.setLayout(new BorderLayout());

        basePanel.setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
        basePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        final JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(BORDER_OFFSET, 2, BORDER_OFFSET, 2));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        passphrasePanel.setBorder(new EmptyBorder(4, 0, 0, 0));
        passphrasePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        final JLabel passphraseTopLabel = new JLabel("Party Passphrase");
        passphraseTopLabel.setForeground(Color.WHITE);
        passphraseTopLabel.setHorizontalTextPosition(JLabel.CENTER);
        passphraseTopLabel.setHorizontalAlignment(JLabel.CENTER);

        final JMenuItem copyOpt = new JMenuItem("Copy Passphrase");
        copyOpt.addActionListener(e ->
        {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(passphraseLabel.getText()), null);
        });

        final JPopupMenu copyPopup = new JPopupMenu();
        copyPopup.setBorder(new EmptyBorder(5, 5, 5, 5));
        copyPopup.add(copyOpt);

        // Add null safety for party passphrase
        String passphrase = plugin.getPartyPassphrase();
        passphraseLabel.setText(passphrase != null ? passphrase : "");
        passphraseLabel.setHorizontalTextPosition(JLabel.CENTER);
        passphraseLabel.setHorizontalAlignment(JLabel.CENTER);
        passphraseLabel.setComponentPopupMenu(copyPopup);

        passphrasePanel.add(passphraseTopLabel);
        passphrasePanel.add(passphraseLabel);
        syncPartyPassphraseVisibility();

        controlsPanel = new ControlsPanel(plugin);
        topPanel.add(controlsPanel);
        topPanel.add(passphrasePanel);

        this.add(topPanel, BorderLayout.NORTH);

        // Wrap content to anchor to top and prevent expansion
        final JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(basePanel, BorderLayout.NORTH);
        final JScrollPane scrollPane = new JScrollPane(northPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        // Setup no members label
        noMembersLabel.setHorizontalAlignment(JLabel.CENTER);
        noMembersLabel.setForeground(Color.GRAY);
    }

    void clearSidebar() {
        basePanel.removeAll();
        playerPanelMap.clear();
        addNoMembersMessage();

        basePanel.revalidate();
        basePanel.repaint();
    }

    /**
     * Shows all members of the party, excluding the local player. See {@link org.partypanelplus.ui.PlayerBanner}
     */
    void renderSidebar() {
        basePanel.removeAll();
        playerPanelMap.clear();

        final List<PartyPlayer> players = plugin.getPartyMembers().values()
                .stream()
                .sorted(Comparator.comparing(o -> Strings.isNullOrEmpty(o.getUsername()) ? o.getMember().getDisplayName() : o.getUsername()))
                .collect(Collectors.toList());

        if (players.isEmpty()) {
            addNoMembersMessage();
        } else {
            for (final PartyPlayer player : players) {
                drawPlayerPanel(player);
            }
        }

        basePanel.revalidate();
        basePanel.repaint();
    }

    private void addNoMembersMessage() {
        basePanel.add(noMembersLabel);
    }

    void drawPlayerPanel(PartyPlayer player) {
        drawPlayerPanel(player, false);
    }

    void drawPlayerPanel(PartyPlayer player, boolean hasBreakingBannerChange) {
        // Remove 'no members' label if it's visible
        basePanel.remove(noMembersLabel);

        PlayerPanel panel = playerPanelMap.get(player.getMember().getMemberId());
        if (panel != null) {
            panel.updatePlayerData(player, true);
            return;
        }

        panel = new PlayerPanel(player, plugin.getConfig(), plugin.spriteManager, plugin.itemManager);
        playerPanelMap.put(player.getMember().getMemberId(), panel);
        panel.updatePlayerData(player, hasBreakingBannerChange);
        basePanel.add(panel);
    }

    void removePartyPlayer(final PartyPlayer player) {
        if (player != null) {
            final PlayerPanel p = playerPanelMap.remove(player.getMember().getMemberId());
            if (p != null) {
                basePanel.remove(p);
                renderSidebar();
            }
        }
    }

    void updatePartyMembersExpand(boolean expand) {
        for (PlayerPanel panel : playerPanelMap.values()) {
            panel.setShowInfo(expand);
            panel.getBanner().setExpandIcon(expand);
            panel.updatePanel();
        }
    }

    public void updatePartyControls() {
        controlsPanel.setVisible(plugin.getConfig().showPartyControls());
    }

    public void syncPartyPassphraseVisibility() {
        String passphrase = plugin.getPartyPassphrase();
        passphraseLabel.setText(passphrase != null ? passphrase : "");
        passphrasePanel.setVisible(plugin.getConfig().showPartyPassphrase() && plugin.isInParty());
    }

    public void updateParty() {
        controlsPanel.updateControls();
        syncPartyPassphraseVisibility();
    }

    public void updateDisplayVirtualLevels() {
        playerPanelMap.values().forEach(PlayerPanel::updateDisplayVirtualLevels);
    }

    public void updateDisplayPlayerWorlds() {
        playerPanelMap.values().forEach(PlayerPanel::updateDisplayPlayerWorlds);
    }
}
