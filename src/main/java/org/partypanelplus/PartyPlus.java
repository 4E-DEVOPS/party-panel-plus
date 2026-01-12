package org.partypanelplus;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.client.party.PartyService;
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

class PartyPlus extends PluginPanel
{
    private final PartyPlusPlugin plugin;

    @Getter
    private final HashMap<Long, PlayerPanel> playerPanelMap = new HashMap<>();

    private final JPanel basePanel = new JPanel();
    private final JPanel passphrasePanel = new JPanel();
    private final JLabel passphraseLabel = new JLabel();
    private final JLabel noMembersLabel = new JLabel("There are no members in your party");

    private final JLabel waitingLabel = new JLabel("Waiting for party members to sync…");

    @Getter
    private final ControlsPanel controlsPanel;

    @Inject
    PartyPlus(final PartyPlusPlugin plugin, final PartyService partyService)
    {
        super(false);
        this.plugin = plugin;
        this.setLayout(new BorderLayout());

        basePanel.setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
        basePanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));

        final JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(BORDER_OFFSET, 2, BORDER_OFFSET, 2));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        passphrasePanel.setBorder(new EmptyBorder(4, 0, 0, 0));
        passphrasePanel.setLayout(new BorderLayout());

        final JPanel passphraseHeaderPanel = new JPanel(new BorderLayout());

        final JLabel passphraseTopLabel = new JLabel("Party Passphrase");
        passphraseTopLabel.setForeground(Color.WHITE);
        passphraseTopLabel.setHorizontalTextPosition(JLabel.CENTER);
        passphraseTopLabel.setHorizontalAlignment(JLabel.CENTER);

        // 🆕 Manual Refresh Button
        final JButton refreshButton = new JButton("⟳");
        refreshButton.setPreferredSize(new Dimension(40, 18));
        refreshButton.setMargin(new Insets(0, 0, 0, 0));
        refreshButton.setToolTipText("Refresh party panel");
        refreshButton.addActionListener(e -> updateParty());

        passphraseHeaderPanel.add(passphraseTopLabel, BorderLayout.CENTER);
        passphraseHeaderPanel.add(refreshButton, BorderLayout.EAST);

        final JMenuItem copyOpt = new JMenuItem("Copy Passphrase");
        copyOpt.addActionListener(e ->
        {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(passphraseLabel.getText()), null);
        });

        final JPopupMenu copyPopup = new JPopupMenu();
        copyPopup.setBorder(new EmptyBorder(5, 5, 5, 5));
        copyPopup.add(copyOpt);

        // ✅ Set tooltip and popup for copy
        passphraseLabel.setToolTipText("Right-click to copy passphrase");
        passphraseLabel.setHorizontalTextPosition(JLabel.CENTER);
        passphraseLabel.setHorizontalAlignment(JLabel.CENTER);
        passphraseLabel.setComponentPopupMenu(copyPopup);

        passphrasePanel.add(passphraseHeaderPanel, BorderLayout.NORTH);
        passphrasePanel.add(passphraseLabel, BorderLayout.CENTER);

        controlsPanel = new ControlsPanel(plugin, partyService);
        topPanel.add(controlsPanel);
        topPanel.add(passphrasePanel);

        this.add(topPanel, BorderLayout.NORTH);

        final JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(basePanel, BorderLayout.NORTH);
        final JScrollPane scrollPane = new JScrollPane(northPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        noMembersLabel.setHorizontalAlignment(JLabel.CENTER);
        noMembersLabel.setForeground(Color.GRAY);

        // 🆕 Setup waiting label
        waitingLabel.setHorizontalAlignment(JLabel.CENTER);
        waitingLabel.setForeground(Color.GRAY);
    }

    void clearSidebar()
    {
        basePanel.removeAll();
        playerPanelMap.clear();
        addNoMembersMessage();
        basePanel.revalidate();
        basePanel.repaint();
    }

    void renderSidebar()
    {
        basePanel.removeAll();
        playerPanelMap.clear();

        final List<PartyPlayer> players = plugin.getPartyMembers().values()
                .stream()
                .sorted(Comparator.comparing(o -> Strings.isNullOrEmpty(o.getUsername()) ? o.getMember().getDisplayName() : o.getUsername()))
                .collect(Collectors.toList());

        if (players.isEmpty())
        {
            if (plugin.isInParty())
            {
                basePanel.add(waitingLabel); // 🆕 show "waiting" if we *should* have data but don't yet
            }
            else
            {
                addNoMembersMessage();
            }
        }
        else
        {
            for (final PartyPlayer player : players)
            {
                drawPlayerPanel(player);
            }
        }

        basePanel.revalidate();
        basePanel.repaint();
    }

    private void addNoMembersMessage()
    {
        basePanel.add(noMembersLabel);
    }

    void drawPlayerPanel(PartyPlayer player)
    {
        drawPlayerPanel(player, false);
    }

    void drawPlayerPanel(PartyPlayer player, boolean hasBreakingBannerChange)
    {
        basePanel.remove(noMembersLabel);
        basePanel.remove(waitingLabel);

        PlayerPanel panel = playerPanelMap.get(player.getMember().getMemberId());
        if (panel != null)
        {
            panel.updatePlayerData(player, true);
            return;
        }

        panel = new PlayerPanel(player, plugin.getConfig(), plugin.spriteManager, plugin.itemManager);
        playerPanelMap.put(player.getMember().getMemberId(), panel);
        panel.updatePlayerData(player, hasBreakingBannerChange);
        basePanel.add(panel);
    }

    void removePartyPlayer(final PartyPlayer player)
    {
        if (player != null)
        {
            final PlayerPanel p = playerPanelMap.remove(player.getMember().getMemberId());
            if (p != null)
            {
                basePanel.remove(p);
                renderSidebar();
            }
        }
    }

    void updatePartyMembersExpand(boolean expand)
    {
        for (PlayerPanel panel : playerPanelMap.values())
        {
            panel.setShowInfo(expand);
            panel.getBanner().setExpandIcon(expand);
            panel.updatePanel();
        }
    }

    public void updatePartyControls()
    {
        controlsPanel.setVisible(plugin.getConfig().showPartyControls());
    }

    public void syncPartyPassphraseVisibility()
    {
        String passphrase = plugin.getPartyPassphrase();
        passphraseLabel.setText(passphrase != null ? passphrase : "");
        passphrasePanel.setVisible(plugin.getConfig().showPartyPassphrase() && plugin.isInParty());
    }

    public void updateParty()
    {
        controlsPanel.updateControls();
        syncPartyPassphraseVisibility();
        renderSidebar();
    }

    public void updateDisplayVirtualLevels()
    {
        playerPanelMap.values().forEach(PlayerPanel::updateDisplayVirtualLevels);
    }

    public void updateDisplayPlayerWorlds()
    {
        playerPanelMap.values().forEach(PlayerPanel::updateDisplayPlayerWorlds);
    }
}
