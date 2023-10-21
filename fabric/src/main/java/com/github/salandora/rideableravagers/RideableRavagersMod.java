package com.github.salandora.rideableravagers;

import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;

public class RideableRavagersMod implements ModInitializer {

	@Override
	public void onInitialize() {
		MidnightConfig.init("rideableravagers", RideableRavagersConfig.class);

		if (RideableRavagersConfig.spawnInSavannah) {
			BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_SAVANNA), MobCategory.MONSTER, EntityType.RAVAGER, RideableRavagersConfig.spawnWeight, RideableRavagersConfig.spawnMinGroupSize, RideableRavagersConfig.spawnMaxGroupSize);
		}
	}
}
