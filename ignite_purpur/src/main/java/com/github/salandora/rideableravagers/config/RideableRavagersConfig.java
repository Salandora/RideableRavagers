package com.github.salandora.rideableravagers.config;

import org.bukkit.configuration.file.YamlConfiguration;
import space.vectrix.ignite.api.Ignite;

import java.io.File;
import java.io.IOException;

public class RideableRavagersConfig {
	public static boolean spawnInSavannah;
	public static int spawnWeight;
	public static int spawnMinGroupSize;
	public static int spawnMaxGroupSize;

	public static void loadConfig() {
		File file  = Ignite.getPlatform().getConfigs().resolve("rideableravagers.yml").toFile();
		if (!file.exists()) {
			saveDefaultConfig(file);
		}

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		spawnInSavannah = config.getBoolean("spawnInSavannah", false);
		spawnWeight = config.getInt("spawnWeight", 20);
		spawnMinGroupSize = config.getInt("spawnMinGroupSize", 1);
		spawnMaxGroupSize = config.getInt("spawnMaxGroupSize", 1);
	}

	private static void saveDefaultConfig(File file) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("spawnInSavannah", false);
		config.set("spawnWeight", 20);
		config.set("spawnMinGroupSize", 1);
		config.set("spawnMaxGroupSize", 1);
		try {
			config.save(file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
