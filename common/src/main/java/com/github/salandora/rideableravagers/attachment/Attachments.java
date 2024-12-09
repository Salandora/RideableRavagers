package com.github.salandora.rideableravagers.attachment;

import com.github.salandora.rideableravagers.RideableRavagers;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public class Attachments {
	public static final AttachmentType<Byte> RAVAGER_FLAGS = EntityAttachment.INSTANCE.create(ResourceLocation.fromNamespaceAndPath(RideableRavagers.MODID, "flags"),
			builder -> builder.initializer(() -> (byte) 0).persistent(Codec.BYTE).synchronize(ByteBufCodecs.BYTE));

	public static final AttachmentType<Optional<UUID>> RAVAGER_OWNER = EntityAttachment.INSTANCE.create(ResourceLocation.fromNamespaceAndPath(RideableRavagers.MODID, "owner"),
			builder -> builder.initializer(Optional::empty).synchronize(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional)));

	public static void init() {
	}
}
