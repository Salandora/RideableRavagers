package com.github.salandora.rideableravagers;

import com.github.salandora.rideableravagers.attachment.Attachments;
import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import eu.midnightdust.lib.config.MidnightConfig;

public class RideableRavagers {
	public static final String MODID = "rideableravagers";

	public static void init() {
		MidnightConfig.init("rideableravagers", RideableRavagersConfig.class);

		Attachments.init();
	}
}
