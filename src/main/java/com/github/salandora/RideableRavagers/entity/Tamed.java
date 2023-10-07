package com.github.salandora.RideableRavagers.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;

public interface Tamed extends Tameable {
	boolean isTamed();

	default boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
		return true;
	}
}
