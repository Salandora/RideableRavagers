package com.github.salandora.rideableravagers.attachment;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class Attachments {
	public static final AttachmentType<Byte> RAVAGER_FLAGS = EntityAttachment.INSTANCE.create(ResourceLocation.fromNamespaceAndPath(RideableRavagers.MODID, "flags"),
			builder -> builder.initializer(() -> (byte) 0).persistent(Codec.BYTE).synchronize(ByteBufCodecs.BYTE));

	public static final AttachmentType<Optional<EntityReference<LivingEntity>>> RAVAGER_OWNER = EntityAttachment.INSTANCE.create(ResourceLocation.fromNamespaceAndPath(RideableRavagers.MODID, "owner"),
			builder -> builder.initializer(Optional::empty).synchronize(EntityReference.<LivingEntity>streamCodec().apply(ByteBufCodecs::optional)));

	public static void init() {
	}
}
