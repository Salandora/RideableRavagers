package com.github.salandora.rideableravagers.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TemptGoal.class)
public interface TemptGoalAccessor {
	@Accessor
	TargetingConditions getTargetingConditions();

	@Invoker("shouldFollow")
	boolean invokeShouldFollow(LivingEntity entity);
}
