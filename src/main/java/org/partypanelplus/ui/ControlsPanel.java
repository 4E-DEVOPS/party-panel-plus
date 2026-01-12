package org.partypanelplus.ui;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.util.Locale;
import org.partypanelplus.PartyPlusPlugin;
import net.runelite.client.party.PartyService;

public class ControlsPanel extends JPanel
{
	private final JButton fcButton = new JButton("Friends Chat");
	private final JButton ccButton = new JButton("Clan Chat");

	private final JTextField customInput = new JTextField();
	private final JButton copyButton = new JButton("⧉");
	private final JButton joinButton = new JButton("➔");

	private final JButton rejoinOrLeaveButton = new JButton();

	private final PartyPlusPlugin plugin;
	private final PartyService partyService;

	@Inject
	public ControlsPanel(PartyPlusPlugin plugin, PartyService partyService)
	{
		this.plugin = plugin;
		this.partyService = partyService;

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 2, 4, 2);

		// === Row 0: FC + CC Buttons ===
		c.gridx = 0;
		c.gridy = 0;
		add(fcButton, c);
		fcButton.setToolTipText("Join Friends Chat party");

		c.gridx = 1;
		add(ccButton, c);
		ccButton.setToolTipText("Join Clan Chat party");

		// === Row 1: Custom Input + Copy/Join ===
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		JPanel customPanel = new JPanel(new BorderLayout(4, 0));
		customInput.setPreferredSize(new Dimension(140, 25));
		customInput.setText(plugin.getConfig().previousPartyId());

		JPanel icons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		icons.add(copyButton);
		copyButton.setToolTipText("Copy the current passphrase");

		icons.add(joinButton);
		joinButton.setToolTipText("Join using the entered passphrase");

		customPanel.add(customInput, BorderLayout.CENTER);
		customPanel.add(icons, BorderLayout.EAST);

		add(customPanel, c);

		// === Row 2: Rejoin / Leave ===
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		add(rejoinOrLeaveButton, c);
		rejoinOrLeaveButton.setToolTipText("Leave current party or rejoin previous one");

		// === Button Actions ===

		fcButton.addActionListener(e -> joinWithSource(plugin.getFriendsChatOwner(), "You are not in a friends chat."));
		ccButton.addActionListener(e -> joinWithSource(plugin.getClanChatOwner(), "You are not in a clan chat."));

		copyButton.addActionListener(e -> {
			String phrase = customInput.getText();
			if (!phrase.isEmpty())
			{
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(phrase), null);
			}
		});

		joinButton.addActionListener(e -> {
			String phrase = customInput.getText().trim().toLowerCase(Locale.US);
			if (!phrase.isEmpty())
			{
				if (plugin.isInParty())
				{
					plugin.leaveParty();
				}

				partyService.changeParty(phrase); // ✅ use PartyService instead of plugin
				plugin.getConfig().setPreviousPartyId(phrase);
			}
		});

		rejoinOrLeaveButton.addActionListener(e -> {
			if (plugin.isInParty())
			{
				int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave the party?", "Leave party?", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION)
				{
					plugin.leaveParty();
				}
			}
			else
			{
				String prev = plugin.getConfig().previousPartyId();
				if (prev != null && !prev.isEmpty())
				{
					partyService.changeParty(prev.toLowerCase(Locale.US)); // ✅ use PartyService
				}
			}
		});

		updateControls();
	}

	private void joinWithSource(String owner, String errorMessage)
	{
		if (owner == null || owner.isEmpty())
		{
			JOptionPane.showMessageDialog(this, errorMessage);
			return;
		}

		if (plugin.isInParty())
		{
			plugin.leaveParty();
		}

		partyService.changeParty(owner.toLowerCase(Locale.US)); // ✅ use PartyService
	}

	public void updateControls()
	{
		rejoinOrLeaveButton.setText(plugin.isInParty() ? "Leave" : "Rejoin");

		fcButton.setEnabled(plugin.getFriendsChatOwner() != null && !plugin.getFriendsChatOwner().isEmpty());
		ccButton.setEnabled(plugin.getClanChatOwner() != null && !plugin.getClanChatOwner().isEmpty());

		setVisible(plugin.getConfig().showPartyControls());
	}
}
