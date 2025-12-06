package com.github.salandora.rideableravagers.data.biome_modifiers;

import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import com.github.salandora.rideableravagers.init.BiomeCodecInit;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record SpawnsBiomeModifier() implements BiomeModifier {
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && RideableRavagersConfig.spawnInSavannah) {
			if (biome.is(BiomeTags.IS_SAVANNA)) {
				builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER,
						new MobSpawnSettings.SpawnerData(EntityType.RAVAGER, RideableRavagersConfig.spawnWeight, RideableRavagersConfig.spawnMinGroupSize, RideableRavagersConfig.spawnMaxGroupSize));
			}
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return BiomeCodecInit.ADD_SPAWNS_CODEC.get();
	}
}
