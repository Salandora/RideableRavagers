package com.github.salandora.rideableravagers.fabric;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class RideableRavagersFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		RideableRavagers.init();

		if (RideableRavagersConfig.spawnInSavannah) {
			BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA), MobCategory.MONSTER, EntityType.RAVAGER, RideableRavagersConfig.spawnWeight, RideableRavagersConfig.spawnMinGroupSize, RideableRavagersConfig.spawnMaxGroupSize);
		}
	}
}
