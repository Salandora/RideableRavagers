package com.github.salandora.rideableravagers.neoforge.init;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.github.salandora.rideableravagers.neoforge.data.biome_modifiers.SpawnsBiomeModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BiomeCodecInit {
	public static DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
			DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, RideableRavagers.MODID);

	public static DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SpawnsBiomeModifier>> ADD_SPAWNS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
			MapCodec.unit(SpawnsBiomeModifier::new));
}
