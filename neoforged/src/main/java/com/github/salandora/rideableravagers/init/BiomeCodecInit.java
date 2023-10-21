package com.github.salandora.rideableravagers.init;

import com.github.salandora.rideableravagers.RideableRavagersMod;
import com.github.salandora.rideableravagers.data.biome_modifiers.SpawnsBiomeModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BiomeCodecInit {
	public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, RideableRavagersMod.MODID);

	public static RegistryObject<Codec<SpawnsBiomeModifier>> ADD_SPAWNS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
			Codec.unit(SpawnsBiomeModifier::new));
}
