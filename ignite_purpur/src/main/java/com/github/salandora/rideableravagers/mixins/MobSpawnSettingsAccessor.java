/*
package com.github.salandora.rideableravagers.mixins;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MobSpawnSettings.class)
public interface MobSpawnSettingsAccessor {
	@Accessor
	Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> getSpawners();

	@Accessor("spawners")
	@Mutable
	void setSpawners(Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawner);

	@Accessor
	Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getMobSpawnCosts();

	@Accessor("mobSpawnCosts")
	@Mutable
	void setMobSpawnCosts(Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> spawnCosts);
}
*/
