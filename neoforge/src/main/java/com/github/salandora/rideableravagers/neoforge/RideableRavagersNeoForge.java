package com.github.salandora.rideableravagers.neoforge;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.github.salandora.rideableravagers.attachment.Attachments;
import com.github.salandora.rideableravagers.neoforge.init.BiomeCodecInit;
import com.github.salandora.rideableravagers.neoforge.networking.SetAttachmentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Ravager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(RideableRavagers.MODID)
public class RideableRavagersNeoForge {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RideableRavagers.MODID);

	public RideableRavagersNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		RideableRavagers.init();

		ATTACHMENT_TYPES.register(modEventBus);

		BiomeCodecInit.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

		modEventBus.addListener(RideableRavagersNeoForge::registerPayloads);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(RideableRavagersNeoForge::onStartTracking);
	}

	public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playToClient(
				SetAttachmentType.TYPE,
				SetAttachmentType.STREAM_CODEC,
				SetAttachmentType::handlePayload
		);
	}

	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof Ravager rav) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			PacketDistributor.sendToPlayer(player,
					SetAttachmentType.create(rav.getId(), Attachments.RAVAGER_FLAGS, rav.getData(Attachments.RAVAGER_FLAGS::attachmentType), player.registryAccess()),
					SetAttachmentType.create(rav.getId(), Attachments.RAVAGER_OWNER, rav.getData(Attachments.RAVAGER_OWNER::attachmentType), player.registryAccess())
			);
		}
	}
}
