package com.github.salandora.rideableravagers.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

public interface Tamable extends OwnableEntity {
	boolean rideableRavagers$isTamed();

	default boolean rideableRavagers$canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		return true;
	}
}
