package com.github.salandora.rideableravagers.attachment.neoforge;

import com.github.salandora.rideableravagers.attachment.AttachmentType;
import com.github.salandora.rideableravagers.attachment.EntityAttachment;

import java.util.List;

public interface AttachmentInterface {
	<T> List<EntityAttachment.OnAttachmentSet<T>> rideableRavagers$onAttachedSet(AttachmentType<T> type);

	<T> void rideableRavagers$invokeOnAttacheSet(AttachmentType<T> type, T oldValue, T newValue);
}
