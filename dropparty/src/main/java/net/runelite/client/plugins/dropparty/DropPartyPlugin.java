/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 *
 * Modified by farhan1666
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.dropparty;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
	name = "Drop Party",
	description = "Marks where a user ran, for drop partys",
	tags = {"Drop", "Party", "marker", "player"},
	type = PluginType.MISCELLANEOUS,
	enabledByDefault = false
)

public class DropPartyPlugin extends Plugin
{
	@Inject
	private DropPartyConfig config;
	@Getter(AccessLevel.PACKAGE)
	private List<WorldPoint> playerPath = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private int MAXPATHSIZE = 100;
	private Player runningPlayer;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DropPartyOverlay coreOverlay;

	@Inject
	private Client client;

	@Provides
	DropPartyConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DropPartyConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(coreOverlay);
		reset();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(coreOverlay);
		reset();
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		shuffleList();
		if (config.playerName().equalsIgnoreCase(""))
		{
			return;
		}

		runningPlayer = null;

		for (Player player : client.getPlayers())
		{
			if (player.getName() == null)
			{
				continue;
			}
			if (Text.standardize(player.getName()).equalsIgnoreCase(config.playerName()))
			{
				runningPlayer = player;
				break;
			}

		}

		if (runningPlayer == null)
		{
			cordsError();
			return;
		}
		addCords();
	}

	private void cordsError()
	{
		playerPath.add(null);
	}

	private void shuffleList()
	{
		if (playerPath.size() > MAXPATHSIZE - 1)
		{
			playerPath.remove(0);
		}
	}

	private void addCords()
	{
		while (true)
		{
			if (playerPath.size() >= MAXPATHSIZE)
			{
				playerPath.add(runningPlayer.getWorldLocation());
				break;
			}
			playerPath.add(null);
		}
	}

	private void reset()
	{
		playerPath.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("dropparty"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			coreOverlay.determineLayer();
			overlayManager.remove(coreOverlay);
			overlayManager.add(coreOverlay);
		}
	}
}