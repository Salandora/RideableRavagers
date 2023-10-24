package com.github.salandora.rideableravagers.mixins;

import com.github.salandora.rideableravagers.config.RideableRavagersConfig;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class BiomeModificationMixin {
	@Shadow
	public abstract RegistryAccess.Frozen registryAccess();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void finalizeWorldGen(CallbackInfo ci) {
		if (!RideableRavagersConfig.spawnInSavannah)
			return;

		Registry<Biome> biomes = registryAccess().registryOrThrow(Registries.BIOME);
		List<ResourceKey<Biome>> keys = biomes.entrySet().stream()
				.map(Map.Entry::getKey)
				.sorted(Comparator.comparingInt(key -> biomes.getId(biomes.getOrThrow(key))))
				.toList();

		for (ResourceKey<Biome> key : keys) {
			Biome biome = biomes.getOrThrow(key);

			if (biomes.getHolderOrThrow(key).is(BiomeTags.IS_SAVANNA)) {
				MobSpawnSettings spawnSettings = biome.getMobSettings();

				// Make mutable list of all existing spawns
				EnumMap<MobCategory, List<MobSpawnSettings.SpawnerData>> mutableSpawners = new EnumMap<>(MobCategory.class);
				for (MobCategory spawnGroup : MobCategory.values()) {
					mutableSpawners.put(spawnGroup, new ArrayList<>(spawnSettings.getMobs(spawnGroup).unwrap()));
				}

				// Make spawn costs mutable
				((MobSpawnSettingsAccessor) spawnSettings).setMobSpawnCosts(new HashMap<>(((MobSpawnSettingsAccessor) spawnSettings).getMobSpawnCosts()));


				// Add custom spawn
				mutableSpawners.get(MobCategory.MONSTER).add(new MobSpawnSettings.SpawnerData(EntityType.RAVAGER, RideableRavagersConfig.spawnWeight, RideableRavagersConfig.spawnMinGroupSize, RideableRavagersConfig.spawnMaxGroupSize));


				// Make spawns immutable again
				Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners = new HashMap<>(((MobSpawnSettingsAccessor) spawnSettings).getSpawners());
				for (Map.Entry<MobCategory, List<MobSpawnSettings.SpawnerData>> entry : mutableSpawners.entrySet()) {
					spawners.put(entry.getKey(), WeightedRandomList.create(entry.getValue()));
				}
				((MobSpawnSettingsAccessor) spawnSettings).setSpawners(ImmutableMap.copyOf(spawners));

				// Make spawn costs immutable again
				((MobSpawnSettingsAccessor) spawnSettings).setMobSpawnCosts(ImmutableMap.copyOf(((MobSpawnSettingsAccessor) spawnSettings).getMobSpawnCosts()));
			}
		}
	}
}
