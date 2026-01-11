package org.partypanelplus.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.util.Locale;
import org.partypanelplus.PartyPlusPlugin;

public class ControlsPanel extends JPanel
{
	private final JButton fcButton = new JButton("Friends Chat");
	private final JButton ccButton = new JButton("Clan Chat");

	private final JTextField customInput = new JTextField();
	private final JButton rerollButton = new JButton("⟳");
	private final JButton copyButton = new JButton("📋");
	private final JButton joinButton = new JButton("➤");

	private final JButton rejoinOrLeaveButton = new JButton();

	private final PartyPlusPlugin plugin;

	public ControlsPanel(PartyPlusPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(4, 2, 4, 2);

		// Row 0: FC + CC
		c.gridx = 0;
		c.gridy = 0;
		add(fcButton, c);

		c.gridx = 1;
		add(ccButton, c);

		// Row 1: Custom input with controls
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		JPanel customPanel = new JPanel(new BorderLayout(4, 0));
		customInput.setPreferredSize(new Dimension(140, 25));
		customInput.setText(plugin.getConfig().previousPartyId());

		JPanel icons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		icons.add(rerollButton);
		icons.add(copyButton);
		icons.add(joinButton);

		customPanel.add(customInput, BorderLayout.CENTER);
		customPanel.add(icons, BorderLayout.EAST);

		add(customPanel, c);

		// Row 2: Rejoin or Leave
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		add(rejoinOrLeaveButton, c);

		// Button Actions
		fcButton.addActionListener(e -> joinWithSource(plugin.getFriendsChatOwner(), "You are not in a friends chat."));
		ccButton.addActionListener(e -> joinWithSource(plugin.getClanChatOwner(), "You are not in a clan chat."));

		rerollButton.addActionListener(e ->
		{
			String random = plugin.generatePartyPassphrase();
			customInput.setText(random);
		});

		copyButton.addActionListener(e ->
		{
			String phrase = customInput.getText();
			if (!phrase.isEmpty())
			{
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(phrase), null);
			}
		});

		joinButton.addActionListener(e ->
		{
			String phrase = customInput.getText().trim().toLowerCase(Locale.US);
			if (!phrase.isEmpty())
			{
				if (plugin.isInParty())
				{
					plugin.leaveParty();
				}
				plugin.changeParty(phrase);
				plugin.getConfig().setPreviousPartyId(phrase); // ✅ Save for rejoin
			}
		});

		rejoinOrLeaveButton.addActionListener(e ->
		{
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
					plugin.changeParty(prev.toLowerCase(Locale.US));
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
		plugin.changeParty(owner.toLowerCase(Locale.US));
	}

	public void updateControls()
	{
		rejoinOrLeaveButton.setText(plugin.isInParty() ? "Leave" : "Rejoin");

		if (!plugin.getConfig().showPartyControls())
		{
			setVisible(false);
		}
		else
		{
			setVisible(true);
		}
	}
}
