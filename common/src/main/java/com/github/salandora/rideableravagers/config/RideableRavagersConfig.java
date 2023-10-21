package com.github.salandora.rideableravagers.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class RideableRavagersConfig extends MidnightConfig {
	@Comment(centered = true)
	public static Comment requiresRestart;
	@Entry
	public static boolean spawnInSavannah = false;

	@Entry(isSlider = true, min = 0, max = 100)
	public static int spawnWeight = 20;

	@Entry
	public static int spawnMinGroupSize = 1;

	@Entry
	public static int spawnMaxGroupSize = 1;
}
