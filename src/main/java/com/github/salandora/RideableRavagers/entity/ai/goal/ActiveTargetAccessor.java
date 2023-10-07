package com.github.salandora.RideableRavagers.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface ActiveTargetAccessor {
	void setPredicate(@Nullable Predicate<LivingEntity> targetPredicate);

	Class<? extends LivingEntity> getTargetClass();
}
