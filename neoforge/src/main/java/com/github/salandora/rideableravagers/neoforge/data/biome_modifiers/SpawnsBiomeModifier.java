package com.github.salandora.rideableravagers.neoforge.data.biome_modifiers;

import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import com.github.salandora.rideableravagers.neoforge.init.BiomeCodecInit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record SpawnsBiomeModifier() implements BiomeModifier {
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && RideableRavagersConfig.spawnInSavannah) {
			if (biome.is(BiomeTags.IS_SAVANNA)) {
				builder.getMobSpawnSettings().addSpawn(
						MobCategory.MONSTER,
						RideableRavagersConfig.spawnWeight,
						new MobSpawnSettings.SpawnerData(EntityType.RAVAGER, RideableRavagersConfig.spawnMinGroupSize, RideableRavagersConfig.spawnMaxGroupSize)
				);
			}
		}
	}

	@Override
	public MapCodec<? extends BiomeModifier> codec() {
		return BiomeCodecInit.ADD_SPAWNS_CODEC.get();
	}
}
