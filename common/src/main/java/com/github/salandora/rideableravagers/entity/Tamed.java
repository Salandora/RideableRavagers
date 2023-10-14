package com.github.salandora.rideableravagers.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

public interface Tamed extends OwnableEntity {
	boolean isTamed();

	default boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		return true;
	}
}
