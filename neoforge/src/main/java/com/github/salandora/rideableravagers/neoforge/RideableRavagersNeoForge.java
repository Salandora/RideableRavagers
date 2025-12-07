package com.github.salandora.rideableravagers.neoforge;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.github.salandora.rideableravagers.client.RideableRavagersClient;
import com.github.salandora.rideableravagers.neoforge.init.BiomeCodecInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(RideableRavagers.MODID)
public class RideableRavagersNeoForge {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RideableRavagers.MODID);

	public RideableRavagersNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		RideableRavagers.init();

		ATTACHMENT_TYPES.register(modEventBus);

		BiomeCodecInit.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

		modEventBus.addListener(RideableRavagersNeoForge::registerAdditionalModelLayers);
	}

	private static void registerAdditionalModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(RideableRavagersClient.RAVAGER_BABY, RideableRavagersClient::createBabyRavagerLayer);
	}
}
