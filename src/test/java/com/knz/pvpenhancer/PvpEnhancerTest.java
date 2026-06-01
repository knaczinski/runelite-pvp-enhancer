package com.knz.pvpenhancer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Development entry point. Launches a full RuneLite client in developer mode with this
 * plugin side-loaded from the classpath. Run via {@code ./gradlew run}.
 *
 * <p>This is the primary development loop — no jar build or copy is required. For using
 * the plugin in a normal RuneLite session, build the jar and side-load it (see
 * {@code docs/building-and-testing.md}).
 */
public class PvpEnhancerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PvpEnhancerPlugin.class);
		RuneLite.main(args);
	}
}
